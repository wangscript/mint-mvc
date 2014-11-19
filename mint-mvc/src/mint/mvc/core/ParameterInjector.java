package mint.mvc.core;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import mint.mvc.converter.ConverterFactory;

import com.alibaba.fastjson.JSON;

/**
 * 参数注射器，负责把前台参数注射入对应的对象内部
 * @author LW
 */
class ParameterInjector {
	/**
	 * parameter's index in action method's parameters.
	 */
	final int 						argumentIndex;
	final Class<?> 					argumentType;
	final String						argumentName;
	final Map<String ,SetterInfo> 	settersMap  	= new HashMap<String ,SetterInfo>();
	
	/*
	 * 基础类型和String 类型和数组不需要注射
	 */
	final boolean 					needInject;
	final boolean					isArray;	
	
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
		
		if(needInject) 
			initSetters();
	}
	
	/**
	 * 将请求参数注射入action参数对象中
	 * @param instance
	 * @param value
	 * @param key the key for access setter method
	 * TODO json框架抛警告，以后一定要干掉
	 */
	@SuppressWarnings("unchecked")
	<T> T  inject(T instance, String value, String key){
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
	Set<String> getKeys(){
		return settersMap.keySet();
	}
	
	/**
	 * 从action参数中分离出请求参数名和对象字段的对应关系
	 * 使用内省获取getter和setter方法
	 */
	private void initSetters(){
		ConverterFactory converter = new ConverterFactory();
		
		/*内省方式获取属性和setter*/
		try {
			PropertyDescriptor[] props = Introspector.getBeanInfo(argumentType, Object.class).getPropertyDescriptors();
			Method setter;
			Class<?> type;
			SetterInfo sInfo;
			for(PropertyDescriptor pd : props){
				type = pd.getPropertyType();
				
				if(converter.canConvert(type)){
					setter = pd.getWriteMethod();
					/*取消虚拟机安全检查，提高方法调用效率*/
					setter.setAccessible(true);
					
					sInfo = new SetterInfo(setter, type, false);
					settersMap.put(argumentName+"."+pd.getName(), sInfo);
				}
			}
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
		
		
		/*把参数本身当成一个可解析项，采用json转换*/
		settersMap.put(argumentName, new SetterInfo(null, null, true));
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
