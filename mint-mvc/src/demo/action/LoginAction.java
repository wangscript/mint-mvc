package demo.action;

import mint.mvc.core.annotation.BaseMapping;
import mint.mvc.core.annotation.Mapping;
import mint.mvc.renderer.TemplateRenderer;
import demo.User;

@BaseMapping("/login")
public class LoginAction {
	
	/**
	 * 拦截器的定义和用法
	 * @param user
	 * @return
	 */
	@Mapping(urls="")
	public TemplateRenderer user(User user){
		System.out.println(user.getId());
		System.out.println(user.getUsername());
		System.out.println(user.getGender());
		
		/*在模板文件中也可以访问到request中的user*/
		return new TemplateRenderer("/login.jsp");
	}
}