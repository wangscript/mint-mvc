package mint.mvc.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.istack.internal.logging.Logger;

import mint.mvc.core.annotation.InterceptorMapping;

/**
 * 
 * 
 * 
 */

/**
 * 
 * Intercept action's execution like servlet Filter, but interceptors are 
 * configured and managed by IoC container. Another difference from Filter 
 * is that Interceptor is executed around Action's execution, but before 
 * rendering view.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:08:52 
 *
 */
public abstract class Interceptor {
	private final static String checkReg = "^((/\\w+)*)[/]?[\\*]?";
	
	private Matcher matcher = null;
	Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * 根据注解初始化url匹配器，并返回成功与否
	 * @return
	 */
	boolean initMatcher(){
		InterceptorMapping intemap = this.getClass().getAnnotation(InterceptorMapping.class);
		
		if(intemap == null){
			logger.warning("拦截器未正确配置InterceptorMapping");
			return false;
		}
		
		String[] urlPattern = intemap.urls();
		matcher = Pattern.compile(checkReg).matcher("");
		
		StringBuilder icptPattern = new StringBuilder();
		String holder = "%%%%%%%%%%%%%%%%%%%%%%";
		
		for(String url : urlPattern){
			
			/*检查url结构是否正确*/
			matcher.reset(url);
			//if(matcher.matches()){
				url = url.replace("**", holder);
				url = url.replace("*", "([^/]*)");
				url = url.replace(holder, "(([^/]+)(/[^/]+)*|[^/]*)");
				icptPattern.append("|").append(url);
			//} else {
				//logger.warning("发现非法拦截器url参数："+url);
			//}
		}
		
		if(icptPattern.length()>1){
			icptPattern.deleteCharAt(0);
			matcher.usePattern(Pattern.compile(icptPattern.toString()));
		} else {
			logger.warning("发现无效拦截器:"+this.getClass().getName());
			return false;
		}
		
		return true;
	}
	
	/**
	 * 当前拦截器是否匹配当前请求的url
	 * @param url
	 * @return
	 */
	boolean matchers(String url){
		return matcher.reset(url).matches();
	}
	
	/**
	 * Do intercept and invoke chain.doInterceptor() to process next interceptor. 
	 * NOTE that process will not continue if chain.doInterceptor() method is not 
	 * invoked.
	 * 
	 * @param chain Interceptor chain.
	 * @throws Exception If any exception is thrown, process will not continued.
	 */
    public abstract void intercept(ActionContext ctx, InterceptorChain chain) throws Exception;

}
