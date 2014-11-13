package demo;

import mint.mvc.core.annotation.BaseMapping;
import mint.mvc.core.annotation.Mapping;

@BaseMapping("/news")
public class NewsAction {
	@Mapping(urls={"/{id}"}, method="get")
	public String index(Integer id, String name){
		System.out.println(name);
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
}
