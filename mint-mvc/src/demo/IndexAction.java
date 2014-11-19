package demo;

import javax.servlet.http.HttpServletRequest;

import mint.mvc.core.annotation.BaseMapping;
import mint.mvc.core.annotation.Mapping;

@BaseMapping("")
public class IndexAction {
	
	@Mapping(urls={""}, method="get")
	public String index(String name, Integer id, User u, HttpServletRequest req){
		System.out.println(name);
		
		//System.out.println(req.getParameter("name"));
		//System.out.println(req.getParameter("id"));
		
		if(u!=null){
			System.out.println(u.getId());
			System.out.println(u.getName());
		}
		return "index";
	}
}
