package demo.action;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import mint.mvc.annotation.BaseMapping;
import mint.mvc.annotation.Mapping;
import demo.Blog;

@BaseMapping("/blog")
public class BlogAction {
	
	
	/**
	 * @param blogId
	 * @return
	 */
	@Mapping(urls="/{blogId:\\\\d+}", method="get")
	public Long getBlog(Long blogId){
		return blogId;
	}
	
	/**
	 * 初始化基础类型参数
	 * @param title
	 * @param content
	 * @return
	 */
	@Mapping(urls="/create", method="post")
	public String createBlog(String title, String content){
		return title+"&"+content;
	}
	
	/**
	 * 初始化数组类型参数(博客摘抄来源)
	 * @param urls
	 * @return
	 */
	@Mapping(urls="/originUrl", method="post")
	public String originUrl(String urls[]){
		if(urls!=null){
			String url = "";
			for(String str : urls){
				url += (str+"<br/>");
			}
			return url;
		} else {
			return "";
		}
	}
	
	/**
	 * 初始化简单bean类参数
	 * @param blog
	 * @return
	 */
	@Mapping(urls="/update", method="post")
	public String updateBlog(Blog blog){
		return blog.getId()+"<br/>"+
				blog.getTitle()+"<br/>"+
				blog.getContent();
	}
	
	
	/**
	 * 用request对象的属性初始化action参数
	 * @param blogId
	 * @param userId
	 * @return
	 */
	@Mapping(urls="/update/{blogId:\\\\d+}", method="get")
	public String deleteBlog(Long blogId, Long userId){
		return "blogId:"+blogId+"<br/>"+
				"userId:"+userId;
	}
	
	/**
	 * 演示初始化内置参数
	 * @param resp
	 */
	@Mapping(urls="/notFound", method="get")
	public void blogNotFound(HttpServletResponse resp){
		try {
			resp.sendRedirect("");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 采用Cookie初始化参数,设置cookie
	 * @param resp
	 */
	@Mapping(urls="/setCookie", method="get")
	public void setCookie(HttpServletResponse resp){
		Cookie cookie = new Cookie("UID", "for login");
		cookie.setPath("/");
		cookie.setMaxAge(3600);
		cookie.setHttpOnly(true);
		resp.addCookie(cookie);
	}
	
	/**
	 * 采用Cookie初始化参数,获取cookie
	 * @param UID
	 * @return
	 */
	@Mapping(urls="/getCookie", method="get")
	public String getCookie(Cookie UID){
		return UID.getValue();
	}
}