package demo;

import javax.servlet.http.HttpServletRequest;
import mint.mvc.core.ActionContext;
import mint.mvc.core.Interceptor;
import mint.mvc.core.InterceptorChain;
import mint.mvc.core.annotation.InterceptorMapping;
import mint.mvc.core.annotation.InterceptorOrder;

@InterceptorOrder(0)
@InterceptorMapping(urls={"/**"})
public class LoginInterceptor extends Interceptor{
	public void intercept(ActionContext ctx, InterceptorChain chain) throws Exception {
		HttpServletRequest request = ctx.getHttpServletRequest();
		request.setAttribute("userId", 2048L);
		chain.doInterceptor(ctx);
	}
}
