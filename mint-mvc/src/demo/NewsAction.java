package demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mint.mvc.core.annotation.BaseMapping;
import mint.mvc.core.annotation.Mapping;
import mint.mvc.renderer.TemplateRenderer;

@BaseMapping("/news")
public class NewsAction {
	@Mapping(urls={"/me/you/her","/{id:\\\\d+}"}, method="get")
	public String index(Integer id, String name){
		return "get";
	}
	
	@Mapping(urls="/{id}", method="post")
	public String post(Long id){
		return "post";
	}
	
	@Mapping(urls="/{id}", method="put")
	public String update(Long id){
		return "update";
	}
	
	@Mapping(urls="/{id}", method="delete")
	public String delete(Long id){
		return "delete";
	}
	
	@Mapping(urls="/users", method="get")
	public TemplateRenderer list(){
		List<User> users = new ArrayList<User>();
		for(int i=0; i<5; i++){
			User user = new User();
			user.setId(i);
			user.setName("name"+1);
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("Users", users);
		
		return new TemplateRenderer("/users.jsp", params);
	}
}
