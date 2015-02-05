package demo;

import mint.mvc.core.ActionContext;
import mint.mvc.core.Interceptor;
import mint.mvc.core.InterceptorChain;
import mint.mvc.core.annotation.InterceptorMapping;
import mint.mvc.core.annotation.InterceptorOrder;

@InterceptorOrder(0)
@InterceptorMapping(urls={"/**/her"})
public class DefaultInterceptor extends Interceptor{
	public void intercept(ActionContext ctx, InterceptorChain chain) throws Exception {
		ctx.getHttpServletRequest().setAttribute("name", "琼羽");
		
		System.out.println("梁威");
		
		chain.doInterceptor(ctx);
	}
}
