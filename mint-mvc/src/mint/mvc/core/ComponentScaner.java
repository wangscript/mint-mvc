package mint.mvc.core;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import mint.mvc.core.annotation.BaseMapping;
import mint.mvc.core.annotation.InterceptorMapping;
import mint.mvc.util.ClassScaner;

public class ComponentScaner {
	private Logger logger = Logger.getLogger(ComponentScaner.class.getName());
	
	public Set<Interceptor> getInteceptorBeans(Config config){
		String param = config.getInitParameter("actionPackages");
		
		if(param != null && !param.equals("")){
			Set<String> componentNames = new HashSet<String>();
			for(String pkg : param.split(";")){
				componentNames.addAll(ClassScaner.getClassnameFromPackage(pkg.trim(), true));
			}
			
			Class<?> clazz;
			Set<Class<?>> interceptorClasses = new HashSet<Class<?>>();
			for(String clsName : componentNames){
				try {
					clazz = Class.forName(clsName, false, this.getClass().getClassLoader()); //避免static语句执行所发生的错误
					if(clazz.getAnnotation(InterceptorMapping.class) != null){
						for(Class<?> parent = clazz.getSuperclass(); parent != null; parent = parent.getSuperclass()){
							if(parent.equals(Interceptor.class)){
								interceptorClasses.add(clazz);
								logger.info("扫描到拦截器->"+clsName);
								break;
							}
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			
			/**/
			Set<Interceptor> interceptors = new HashSet<Interceptor>();
			Interceptor itcep = null;
			for(Class<?> cls : interceptorClasses){
				try {
					itcep = (Interceptor) cls.newInstance();
					if(itcep.initMatcher()){
						interceptors.add(itcep);
					}
				} catch (InstantiationException | IllegalAccessException e) {
					logger.warning("无法初始化拦截器:"+cls.getName());
				}
			}
			
			return interceptors;
		}
		
		return null;
	}
	
	/**
	 * Find all beans in container.
	 */
	public Set<Object> getActionBeans(Config config){
		String param = config.getInitParameter("actionPackages");
		
		if(param != null && !param.equals("")){
			Set<Class<?>> actionClasses = new HashSet<Class<?>>();
			String[] packages = param.split(";");
			Set<String> componentNames = new HashSet<String>();
			for(String pkg : packages){
				componentNames.addAll(ClassScaner.getClassnameFromPackage(pkg.trim(), true));
			}
			
			Class<?> clazz;
			for(String clsName : componentNames){
				try {
					clazz = Class.forName(clsName, false, ComponentScaner.class.getClassLoader()); //避免static语句执行所发生的错误
					if(clazz.getAnnotation(BaseMapping.class) != null){
						actionClasses.add(clazz);
						logger.info("扫描到Action->"+clsName);
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			
			Set<Object> actions = new HashSet<Object>();
			for(Class<?> cls : actionClasses){
				try {
					actions.add(cls.newInstance());
				} catch (InstantiationException | IllegalAccessException e) {
					logger.warning("无法初始化action:"+cls.getName());
				}
			}
			return actions;
		}
		
		return null;
	}
}
