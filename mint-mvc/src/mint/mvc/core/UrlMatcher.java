package mint.mvc.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mint.mvc.converter.ConverterFactory;
import mint.mvc.util.GetArgumentName;

import com.sun.istack.internal.logging.Logger;

/**
 * Match URL by regular expression<br/>
 * The maximum number of parameters is 10(from 0 to 10).
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LW
 */
final class UrlMatcher {
	static final String[] 	EMPTY_STRINGS 	= new String[0];
	static final String 	SAFE_CHARS 		= "/$-_.+!*'(),";
	private final Logger logger = Logger.getLogger(this.getClass());
    final String  url;
    /**
     * 匹配url的正则
     */
    final Pattern 	pattern;
    final int[] 	urlArgumentOrder;

    /**
     * Build UrlMatcher by given url like "/blog/{name}/{id}".
     * 
     * @param url Url may contains {name}, {id}, ... {..}.
     */
    UrlMatcher(String url, Method actionMethod) {
    	List<String> argumentNames 	= GetArgumentName.getArgumentNames(actionMethod);
        this.url 				= url;
        Matcher matcher 		= Pattern.compile("\\{\\w+\\}").matcher(url);
    	Set<String> paramSet 	= new HashSet<String>();
    	List<String> paramList 	= new ArrayList<String>();
    	String urlParameterName;
    	
    	while (matcher.find()) {
    		urlParameterName = matcher.group(0).replace("{", "").replace("}", "");
    		paramSet.add(urlParameterName);
    		paramList.add(urlParameterName);
    	}
    	/*检查url有没有包含相同参数名*/
        if (paramSet.size() != paramList.size()) {
            throw new ConfigException("uri包含同名参数");
        }
    	
        this.urlArgumentOrder = new int[paramSet.size()];
        boolean existArgument = false;
        for(String paramName : paramList){
        	for(int i=0; i<argumentNames.size(); i++){
        		if(argumentNames.get(i).equals(paramName)){
        			urlArgumentOrder[i] = i;
        			existArgument = true;
        		}
        	}
        	
        	/*如果url中的参数名在action方法中找不到*/
        	if(!existArgument){
        		throw new ConfigException("action 方法:" + actionMethod.toGenericString() + " 不含有" + paramName + "参数");
        	}
    	}
        
        if(checkIsActionMethod(actionMethod)){
        	matcher.reset();
        	StringBuffer sb = new StringBuffer();
        	while (matcher.find()) {
        		matcher.appendReplacement(sb, "(\\\\w+)");
        	}
        	matcher.appendTail(sb);
        	
        	/* "/user/name" 和 "/user/name/" 都可以匹配 */
        	this.pattern = Pattern.compile(sb.toString()+"[/]?");
        } else {
        	this.pattern = null;
        }
    }

    private boolean checkIsActionMethod(Method method){
    	 /*check if the url argument type can be convert*/
        Class<?>[] argTypes = method.getParameterTypes();
        ConverterFactory converterFactory 	= new ConverterFactory();
        
        Class<?> argType;
        for (int argIndex : urlArgumentOrder) {
        	argType = argTypes[argIndex];
            if (!converterFactory.canConvert(argTypes[argIndex])) {
            	logger.warning(method.toGenericString() + "不支持的uri参数类型" + argType.getName() + ":uri参数类型只能为基础类型或者String类型");
                return false;
            }
            argTypes[argIndex] = null;
        }
        
        /*
         * url参数必须为基础类型，比如int, long etc.
         * 非url参数不许为基础类型。这主要是为了防止 参数空指针的问题
         */
        for(Class<?> type : argTypes){
        	if(type != null){
        		if(type.isPrimitive()){
        			throw new ConfigException(method.toGenericString() + "除了uri参数之外，所有action方法参数都不能是基础类型");
        		}
        	}
        }
		return true;
    }
    
    /**
     * Test if the url is match the regex. If matched, the parameters are 
     * returned as String[] array, otherwise, null is returned.
     * 
     * @param url The target url.
     * @return String[] array or null if url is not match.
     */
    String[] getUrlParameters(String url) {
        Matcher m = pattern.matcher(url);
        if (!m.matches()){
            return null;
        } if (urlArgumentOrder.length == 0){
            return EMPTY_STRINGS;
        }
        String[] params = new String[urlArgumentOrder.length];
        for (int i=0; i<urlArgumentOrder.length; i++) {
            params[i] = m.group(i+1);
        }
        return params;
    }
    
    public Map<Integer, String> getUrlParameters1(String url){
    	return null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj==this)
            return true;
        if (obj instanceof UrlMatcher) {
            return ((UrlMatcher)obj).url.equals(this.url);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

}