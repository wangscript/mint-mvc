package demo;

import javax.servlet.http.HttpServletRequest;

import mint.mvc.annotation.InterceptorMapping;
import mint.mvc.annotation.InterceptorOrder;
import mint.mvc.core.ActionContext;
import mint.mvc.core.Interceptor;
import mint.mvc.core.InterceptorChain;

/** 
 * 拦截器的定义和用法
 * @author LiangWei(895925636@qq.com)
 * @date 2015年5月2日 下午1:51:58 
 *  
 */
@InterceptorOrder(0)
@InterceptorMapping(urls={"/**"})
public class LoginInterceptor extends Interceptor{
    public void intercept(ActionContext ctx, InterceptorChain chain) throws Exception {
        HttpServletRequest request = ctx.getHttpServletRequest();
        User user = new User();
        user.setId("8888");
        user.setUsername("haiying");
        user.setGender(0);
        
        request.setAttribute("user", user);
        
        chain.doInterceptor(ctx);
    }
}