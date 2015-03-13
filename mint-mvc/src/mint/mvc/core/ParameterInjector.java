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
 * @Description: 参数注射器，负责把前台参数注射入对应的对象内部
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:09:34 
 *
 */
class ParameterInjector {
	/**
	 * parameter's index in action method's parameters.
	 */
	final int 						argIndex;
	final Class<?> 					argType;
	final String					argName;
	final Map<String ,SetterInfo> 	settersMap  	= new HashMap<String ,SetterInfo>();
	
	/*
	 * 基础类型和String 类型和数组不需要注射
	 */
	final boolean 					needInject;
	final boolean					isArray;	
	
	ParameterInjector(int argIndex, Class<?> argType, String argName){
		this.argIndex = argIndex;
		this.argType = argType;
		this.argName = argName;
		
		isArray = argType.isArray();
		
		if(argType.isPrimitive() || argType.equals(String.class) || isArray){
			needInject = false;
		} else {
			boolean result;
			try {
				result = !((Class<?>)argType.getField("TYPE").get(null)).isPrimitive();
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
	 * TODO json框架抛警告，以后一定要干掉
	 */
	@SuppressWarnings("unchecked")
	<T> T  inject(T instance, String value, String key){
		SetterInfo s = settersMap.get(key);
		/*JSON to OBJECT*/
		if(s.isJSON){
			return (T) JSON.toJavaObject(JSON.parseObject(value), argType);
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
		if(needInject){
			ConverterFactory converter = new ConverterFactory();
			
			/*内省方式获取属性和setter*/
			try {
				PropertyDescriptor[] props = Introspector.getBeanInfo(argType, Object.class).getPropertyDescriptors();
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
						settersMap.put(argName+"."+pd.getName(), sInfo);
					}
				}
			} catch (IntrospectionException e) {
				e.printStackTrace();
			}
			
			/*把参数本身当成一个可解析项，采用json转换*/
			settersMap.put(argName, new SetterInfo(null, null, true));
		} else {
			settersMap.put(argName, null);
			if(isArray){
				settersMap.put(argName+"[]", null);
			}
		}
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
