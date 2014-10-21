package mint.mvc.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mint.mvc.converter.ConverterFactory;

import com.alibaba.fastjson.JSON;

/**
 * 参数注射器，负责把前台参数注射入对应的对象内部
 * @author LW
 */
public class ParameterInjector {
	/**
	 * parameter's index in action method's parameters.
	 */
	protected final int 						argumentIndex;
	protected final Class<?> 					argumentType;
	protected final String						argumentName;
	protected final Map<String ,SetterInfo> 	settersMap  	= new HashMap<String ,SetterInfo>();
	
	/*
	 * 基础类型和String 类型和数组不需要注射
	 */
	protected final boolean 					needInject;
	protected final boolean						isArray;	
	
	ParameterInjector(int argumentIndex, Class<?> argumentType, String argumentName){
		this.argumentIndex = argumentIndex;
		this.argumentType = argumentType;
		this.argumentName = argumentName;
		
		isArray = argumentType.isArray();
		
		if(argumentType.isPrimitive() || argumentType.equals(String.class) || isArray){
			needInject = false;
		} else {
			boolean result = true;
			try {
				result = !((Class<?>)argumentType.getField("TYPE").get(null)).isPrimitive();
			} catch (Exception e) {
				result = true;
			}
			
			needInject = result;
		}
		
		initSetters();
	}
	
	/**
	 * 将请求参数注射入action参数对象中
	 * @param instance
	 * @param value
	 * @param key the key for access setter method
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T> T  inject(T instance, String value, String key){
		SetterInfo s = settersMap.get(key);
		/*JSON to OBJECT*/
		if(s.isJSON){
			return	 (T) JSON.toJavaObject(JSON.parseObject(value), argumentType);
		}
		
		try {
			s.setter.invoke(instance, (new ConverterFactory()).convert(s.fieldType, value));
		} catch (Exception e) {	
			e.printStackTrace();
		}
		
		return instance;
	}
	
	/**
	 * @return keys to access current injector
	 */
	public Set<String> getKeys(){
		return settersMap.keySet();
	}
	
	/**
	 * 从action参数中分离出请求参数名和对象字段的对应关系
	 */
	private void initSetters(){
		if(needInject){
			ConverterFactory converter = new ConverterFactory();
			
			List<Field> fields = new LinkedList<Field>();
			Class<?> clazz = argumentType;
			while(clazz != null){
				for(Field field : clazz.getDeclaredFields()){
					fields.add(field);
				}
				clazz = clazz.getSuperclass();
			}
			
			Method setter;
			String setterName;
			SetterInfo sInfo;
			for(Field f : fields){
				if(converter.canConvert(f.getType())){
					setterName = "set"+firstCharToUpperCase(f.getName());
					try {
						setter = argumentType.getMethod(setterName, f.getType());
						/*取消虚拟机安全检查，提高方法调用效率*/
						setter.setAccessible(true);
						sInfo = new SetterInfo(setter, f.getType(), false);
						settersMap.put(argumentName+"."+f.getName(), sInfo);
					} catch (NoSuchMethodException e) {	}
				}
			}
			/*把本身当成一个可解析项，采用json转换*/
			settersMap.put(argumentName, new SetterInfo(null, null, true));
		} else {
			settersMap.put(argumentName, null);
			if(isArray){
				settersMap.put(argumentName+"[]", null);
			}
		}
	}
	
	private String firstCharToUpperCase(String s){
		if(s.equals("") || s == null) return s;
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}
}

/**
 * @author LW
 * 暂存setter的信息
 */
class SetterInfo {
	public final boolean 	isJSON;
	public final Method 	setter;
	public final Class<?>	fieldType;
	
	SetterInfo(Method setter, Class<?> fieldType, boolean isJSON){
		this.isJSON		= isJSON;
		this.setter 	= setter;
		this.fieldType 	= fieldType;
	}
}
