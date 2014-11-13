package mint.mvc.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mint.mvc.core.annotation.MultipartConfig;
import mint.mvc.core.annotation.ReturnJson;
import mint.mvc.util.GetArgumentName;

/**
 * Internal class which holds object instance, method and arguments' types.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LW
 */
class ActionConfig {
	static Logger logger = Logger.getLogger(ActionConfig.class.getName());
	
	/**
	 * 声明的内置变量
	 */
	static final Class<?>[] builtInObjects = {HttpServletRequest.class, HttpServletResponse.class, HttpSession.class, Cookie.class};
	
    /**
     * Object instance.
     */
    final Object instance;

    /**
     * Method instance.
     */
    final Method actionMethod;
    
    final boolean isMultipartAction;
    
    final MultipartConfig multipartConfig;
    /**
     * 记录action是否声明返回json
     */
    final boolean returnJson;
    
    /**
     * Method's arguments' types.
     */
    final Class<?>[] argumentTypes;
    
    /**
     * 
     */
    final int[] urlArgumentOrder;
    
    /**
     * Method's arguments' names.
     */
    final List<String> 	argumentNames;
    
    /**
     * 参数注射器
     */
    final Map<String ,ParameterInjector> injectors = new HashMap<String ,ParameterInjector>();
    
    Map<Class<?>, Integer> builtInArgument = null;
    
    ActionConfig(Object instance, Method actionMethod, int[] urlArgumentOrder) {
        this.instance 		= instance;
        this.actionMethod 	= actionMethod;
        this.argumentTypes 	= actionMethod.getParameterTypes();
        this.argumentNames	= GetArgumentName.getArgumentNames(actionMethod);
        this.urlArgumentOrder = urlArgumentOrder;
        
        if(actionMethod.getAnnotation(MultipartConfig.class) != null){
        	multipartConfig = actionMethod.getAnnotation(MultipartConfig.class);
        	boolean is = true;
        	if("".equals(multipartConfig.attributeName())){
        		is = false;
        		logger.warning(actionMethod.getName() + ":多媒体请求没有配置 attributeName");
        	}
        	
        	if("".equals(multipartConfig.tempFilePath())){
        		is = false;
        		logger.warning(actionMethod.getName() + ":多媒体请求没有配置 文件临时保存目录");
        	}
        	
        	if(multipartConfig.limitSize() <= 0){
        		is = false;
        		logger.warning(actionMethod.getName() + ":多媒体请求没有配置 正确的limitSize");
        	}
        	
        	this.isMultipartAction = is;
        } else {
        	this.isMultipartAction = false;
        	multipartConfig = null;
        }
        
        if(actionMethod.getAnnotation(ReturnJson.class) != null){
        	returnJson = true;
        } else {
        	returnJson = false;
        }
        
        /*取消虚拟机安全检查，大幅提高方法调用效率*/
        this.actionMethod.setAccessible(true);
        initInjector();
    }

    /**
     * 为action方法初始化参数注射器（请求参数->java Object）
     */
    private void initInjector(){
    	ParameterInjector injector;
    	Set<String>	keys;
    	Class<?> type;
    	for(int i=0 ;i<argumentTypes.length ;i++){
    		type = argumentTypes[i];
    		/*
    		 * 内置参数
    		 * 包括Cookie数组、HttpServletRequest、HttpServletResponse、Session
    		 */
    		if(type.equals(Cookie[].class) || type.equals(HttpSession.class) || type.equals(HttpServletRequest.class) || type.equals(HttpServletResponse.class)){
    			if(builtInArgument == null) builtInArgument = new HashMap<Class<?>, Integer>(); 
    			builtInArgument.put(type, i);
    			continue;
    		}

    		injector = new ParameterInjector(i, type, argumentNames.get(i));
			keys = injector.getKeys();
			for(String key : keys){
				injectors.put(key, injector);
			}
    	}
    }
}