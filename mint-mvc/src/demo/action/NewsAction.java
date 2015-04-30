package demo.action;

import mint.mvc.core.annotation.BaseMapping;
import mint.mvc.core.annotation.Mapping;

@BaseMapping("/news")
public class NewsAction {
	
	
	/**
	 * id的正则限制了只能匹配数字。":"后面是正则表达式，规定可接收的字符串
	 * @param id
	 * @param name
	 * @return
	 */
	@Mapping(urls={"/me/you/her","/{id:\\\\d+}"}, method="get")
	public String index(Integer id, String name){
		return "get";
	}
	
	/**
	 * @param id
	 * @return
	 */
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
