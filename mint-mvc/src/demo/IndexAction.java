package demo;

import mint.mvc.core.annotation.BaseMapping;
import mint.mvc.core.annotation.Mapping;

@BaseMapping("")
public class IndexAction {
	
	@Mapping(urls={"", "/{index:blog|article}"}, method="get")
	public String index(String fileName){
		return fileName;
	}
}
