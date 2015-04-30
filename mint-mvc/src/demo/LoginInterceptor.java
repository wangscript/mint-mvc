package demo;

import javax.servlet.http.HttpServletRequest;

import mint.mvc.core.ActionContext;
import mint.mvc.core.Interceptor;
import mint.mvc.core.InterceptorChain;
import mint.mvc.core.annotation.InterceptorMapping;
import mint.mvc.core.annotation.InterceptorOrder;

/**
 * @author LW
 */
@InterceptorOrder(0)
@InterceptorMapping(urls={"/test/login"})
public class LoginInterceptor extends Interceptor{
	public void intercept(ActionContext ctx, InterceptorChain chain) throws Exception {
		HttpServletRequest request = ctx.getHttpServletRequest();
		User user = new User();
		user.setUsername("琼羽");
		user.setPassword("somebody");
		
		request.setAttribute("user", user);
		chain.doInterceptor(ctx);
	}
}
