package demo.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import mint.mvc.core.annotation.BaseMapping;
import mint.mvc.core.annotation.Mapping;
import mint.mvc.core.annotation.ReturnJson;
import mint.mvc.renderer.TemplateRenderer;
import demo.User;

/**
 * @author LW
 */
@BaseMapping("/test")
public class DemoAction {
	
	@Mapping(urls="/index", method="get")
	public String index(){
		return "helloworld";
	}
	
	@Mapping(urls="/book/{bookId}")
	public Integer login(int bookId){
		return bookId;
	}
	
	@Mapping(urls="/book", method="post")
	public String blog(String bookName, Integer bookId){
		return bookName+"|"+bookId;
	}
	
	@Mapping(urls="/books")
	public String blogs(String[] bookTitles){
		String result = "";
		if(bookTitles != null){
			for(String title : bookTitles){
				result += (title+" | ");
			}
		}
		
		return result;
	}
	
	@Mapping(urls="/logout")
	public void logout(HttpServletResponse response){
		try {
			response.sendRedirect("index");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Mapping(urls="/string")
	public User String(){
		return new User();
	}
	
	@Mapping(urls="/user")
	public TemplateRenderer user(){
		User user = new User();
		user.setUsername("琼羽");
		user.setPassword("1234");
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("user", user);
		param.put("gender", "女");
		return new TemplateRenderer("/user.jsp", param);
	}
	
	@ReturnJson
	@Mapping(urls="/json")
	public User json(){
		User user = new User();
		user.setUsername("琼羽");
		user.setPassword("123");
		
		return user;
	}
}
