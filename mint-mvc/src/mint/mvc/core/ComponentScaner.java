package mint.mvc.core;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import mint.mvc.annotation.BaseMapping;
import mint.mvc.annotation.InterceptorMapping;
import mint.mvc.util.ClassScaner;

/**
 * 
* 组件的扫描器。根据web.xml配置的"actionPackages"启动参数，自动扫描出action和interceptor
* @author LiangWei 
* @date 2015年3月13日 下午7:37:45 
*
 */
class ComponentScaner {
	private Logger logger = Logger.getLogger(ComponentScaner.class.getName());
	
	Set<Interceptor> getInteceptorBeans(Config config){
		ClassScaner sc = new ClassScaner(config.getClass().getClassLoader());
		
		String param = config.getInitParameter("actionPackages");
		
		if(param != null && !param.equals("")){
			Set<String> componentNames = new HashSet<String>();
			for(String pkg : param.split(";")){
				componentNames.addAll(sc.getClassnameFromPackage(pkg.trim(), true));
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
								logger.info("discover a interceptor->"+clsName);
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
					logger.warning("discover a interceptor->"+cls.getName());
				}
			}
			
			return interceptors;
		}
		
		return null;
	}
	
	/**
	 * Find all beans in container.
	 */
	Set<Object> getActionBeans(Config config){
		String param = config.getInitParameter("actionPackages");
		ClassScaner sc = new ClassScaner(config.getClass().getClassLoader());
		
		if(param != null && !param.equals("")){
			Set<Class<?>> actionClasses = new HashSet<Class<?>>();
			String[] packages = param.split(";");
			Set<String> componentNames = new HashSet<String>();
			for(String pkg : packages){
				componentNames.addAll(sc.getClassnameFromPackage(pkg.trim(), true));
			}
			
			Class<?> clazz;
			for(String clsName : componentNames){
				try {
					clazz = Class.forName(clsName, false, ComponentScaner.class.getClassLoader()); //避免static语句执行所发生的错误
					if(clazz.getAnnotation(BaseMapping.class) != null){
						actionClasses.add(clazz);
						logger.info("discover a action->"+clsName);
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
					logger.warning("can't instantiates action->"+cls.getName());
				}
			}
			return actions;
		}
		
		return null;
	}
}
