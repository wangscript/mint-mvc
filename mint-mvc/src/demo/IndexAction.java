package demo;

import mint.mvc.core.annotation.BaseMapping;
import mint.mvc.core.annotation.Mapping;

@BaseMapping("")
public class IndexAction {
	
	@Mapping(urls={"", "/{test}/{index:blog|article}"}, method="get")
	public String index(String fileName, String test){
		return fileName+" "+test;
	}
}
