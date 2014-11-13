package demo;

import mint.mvc.core.annotation.BaseMapping;
import mint.mvc.core.annotation.Mapping;

@BaseMapping("")
public class IndexAction {
	
	@Mapping(urls={"", "/index/{id}"}, method="get")
	public String index(Integer id, String name){
		System.out.println(name);
		return "index";
	}
}
