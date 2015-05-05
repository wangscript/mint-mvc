package demo.action;

import mint.mvc.annotation.BaseMapping;
import mint.mvc.annotation.Mapping;

@BaseMapping("/")
public class IndexAction {
	
	/**
	 * "/index" 和 "/"都可以访问到此方法
	 * @return
	 */
	@Mapping(urls={"index", ""}, method="get")
	public String index(){
		return "hollow mint-mvc";
	}
}