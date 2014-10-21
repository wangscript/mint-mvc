package mint.mvc.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotation for mapping URL.<br/>
 * For example:<br/>
 * <pre>
 *	@InterceptorOrder(0)
 *	@InterceptorMapping(urls="/user/*")
 *	public class DefaultInterceptor implements Interceptor{
 *		@Override
 *		public void intercept(ActionContext ctx, InterceptorChain chain) throws Exception {
 *		}
 *	}
 * </pre>
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface InterceptorMapping {
    String[] urls();
}