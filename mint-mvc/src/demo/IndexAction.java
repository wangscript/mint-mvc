package demo;

import mint.mvc.core.annotation.BaseMapping;
import mint.mvc.core.annotation.Mapping;

@BaseMapping("")
public class IndexAction {
	
	@Mapping(urls={"", "/index/{id}", "/index/{id}/{name}"}, method="get")
	public String index(String name, Integer id, User u){
		if(u!=null){
			System.out.println(u.getId());
			System.out.println(u.getName());
		}
		return "index";
	}
}
