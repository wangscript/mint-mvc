package mint.mvc.core;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sun.istack.internal.logging.Logger;

import mint.mvc.core.annotation.BaseMapping;
import mint.mvc.core.annotation.Mapping;

/**
 * action detector,used for finding out all action methods from beans by Mapping annotation.
 * action 探测器。用来从指定实体中找到多有的action实体，并找到所有的action 方法（带Mapping）的方法
 * @author LW
 */
public class ActionDetector {
	Logger logger = Logger.getLogger(this.getClass());
    private Map<UrlMatcher, ActionConfig> getUrlMap 	= new HashMap<UrlMatcher, ActionConfig>();
    private Map<UrlMatcher, ActionConfig> postUrlMap 	= new HashMap<UrlMatcher, ActionConfig>();
    
	/**
	 * find out action methods from given beans.
     * @param beans
     * @return
     */
    public void awareActionMethodFromBeans(Set<Object> beans) {
    	for(Object bean : beans){
    		awareActionFromBean(bean);
    	}
    }
    
    /**
     * find out action methods from single bean.
     * @param actionBean
     * @return
     */
    private void awareActionFromBean(Object actionBean){
    	Class<?> clazz = actionBean.getClass();
        String 	baseUrl = clazz.getAnnotation(BaseMapping.class).value();
        
        /*一个url匹配器和一个action组成键值对*/
        /*"UrlMatcher=>Action" key-value*/
        Method[]	methods 	= clazz.getMethods();
        Mapping		mapping		= null;
        String[]	urls 		= null;
        for (Method method : methods) {
            if (isActionMethod(method)) {
            	mapping = method.getAnnotation(Mapping.class);
            	urls = mapping.urls();
            	
            	for(String url : urls){
            		url = baseUrl + url;
            		
            		UrlMatcher 	matcher = new UrlMatcher(url, method);
            		/*如果pattern为空，则说明该action方法无法被访问到*/
            		if(matcher.pattern != null){
            			logger.info("Mapping url '" + url + "' to method '" + method.toGenericString() + "'.");
            			addAction(matcher, new ActionConfig(actionBean, method, matcher.urlArgumentOrder), mapping.method());
            			
            			/*如果该action类有index处理方法，就将该类的根url匹配到index方法*/
            			if(url.equals(baseUrl+"/index")){
            				logger.info("Mapping url '" + baseUrl + "' to method '" + method.toGenericString() + "'.");
            				addAction(new UrlMatcher(baseUrl, method), new ActionConfig(actionBean, method, matcher.urlArgumentOrder), mapping.method());
            			}
            		}
            	}
            }
        }
    }
    
    /**
     * @param matcher
     * @param action
     * @param method
     */
    private void addAction(UrlMatcher matcher, ActionConfig action, String method){
    	if("".equals(method)){
    		getUrlMap.put(matcher, action);
    		postUrlMap.put(matcher, action);
    	} else if("get".equals(method.toLowerCase())){
    		getUrlMap.put(matcher, action);
    	} else if("post".equals(method.toLowerCase())){
    		postUrlMap.put(matcher, action);
    	} else {
    		logger.warning("unsupport request method:" + method + ".");
    	}
    }
    
    /**
     * 初步检查方法是否为action方法
     * check if the specified method is a vaild action method:
     * @param method
     * @return
     */
    boolean isActionMethod(Method method) {
    	/*静态方法不是action方法*/
    	if (Modifier.isStatic(method.getModifiers())) {
    		warnInvalidActionMethod(method, "method is static.");
    		return false;
    	}
    	
    	/*没有Mapping 注解的不是action方法*/
        Mapping mapping = method.getAnnotation(Mapping.class);
        if (mapping == null) {
            return false;
        }
        
        return true;
    }
    
    public Map<UrlMatcher, ActionConfig> getGetUrlMap() {
    	return getUrlMap;
    }
    
    public Map<UrlMatcher, ActionConfig> getPostUrlMap() {
    	return postUrlMap;
    }
    
    //log warning message of invalid action method:
    void warnInvalidActionMethod(Method m, String string) {
    	logger.warning("Invalid Action method '" + m.toGenericString() + "': " + string);
    }
}