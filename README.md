mint-mvc是一个注重用户体验的java mvc框架，直面web2.0开发，简单易用，功能完备，支持restful，采用全面采用annotation配置，只需要一个配置文件——web.xml。mint-mvc做的事情很简单：接收请求->封装参数->将请求交给开发者这编写的逻辑处理->返回处理结果。

#features
##lightweight
基于servlet的浅层封装，结构紧凑，设计精巧；只有一个配置文件:web.xml,且配置项不超过十项；java文件52个，代码2500多行，jar包80KB；只有一个第三方依赖
##smart
客户端的请求参数被自动封装成相应的类，用于初始化action的参数。支持丰富的参数初始化方式，大大简化参数和常用对象的获取过程
##Supports RESTful
直观简短的资源地址；Web服务接受与返回的互联网媒体类型，比如：JSON，XML 等；Web服务在资源上支持POST，GET，PUT或DELETE方法的操作
##Strong expansion
接口体系结构。异常监听器接口实现灵活的异常处理逻辑；模板渲染接口可以扩展支持更多的模板引擎；拦截器接口自定义AOP逻辑，满足开发者的定制欲望

#quickstart demo
```java
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
	 * @param blogId accepts only numbers
	 * @return
	 */
	@Mapping(urls="/{blogId:\\\\d+}", method="get")
	public Long getBlog(Long blogId){
		return blogId;
	}
	
	/**
	 * Initialize the base type parameters
	 * @param title
	 * @param content
	 * @return
	 */
	@Mapping(urls="/create", method="post")
	public String createBlog(String title, String content){
		return title+"&"+content;
	}
	
	/**
	 * To initialize an array type arguments (excerpt from the blog)
	 * accepts parameters like "urls=url1?urls=url2" or "urls[]=url1?urls[]=url2"
	 *
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
	 * Initializes a simple bean class argument
	 * accepts parameters like "blog.id=123&blog.title=hi,friends"
	 *
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
	 * Initializes arguments with request's attributes.
	 * Before that(usually in a Filter or a Interceptor), put some attributes into
	 * request like this "request.setAttribute("blogId", 1024L);request.setAttribute("userId", 2048L);"
	 *
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
	 * Initialize the built-in parameters.
	 * built-in parameter includes HttpServletRequest,HttpServletResponse,Session,Cookie[]
	 *
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
	 * Initialize a cookie
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
	 * get the cookie
	 *
	 * @param UID
	 * @return
	 */
	@Mapping(urls="/getCookie", method="get")
	public String getCookie(Cookie UID){
		return UID.getValue();
	}
}
```
#web.xml config
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns="http://java.sun.com/xml/ns/javaee" 
	xmlns:jsp="http://java.sun.com/xml/ns/javaee/jsp"
	xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
	version="3.0">
	
	<servlet>
		<servlet-name>dispatcher</servlet-name>
		<servlet-class>mint.mvc.core.ServiceServlet</servlet-class>
		
		<!-- 静态文件缓存设置，非必要。字符串类型，默认为"max-age=600"。 -->
		<init-param>
			<param-name>staticFileCacheControl</param-name>
			<param-value>max-age=1800</param-value>
		</init-param>
		
		<!-- 处理静态文件是否向服务器询问文件的修改时间，非必要。boolean类型，默认为true。 -->
		<init-param>
			<param-name>staticFileLastModifiedCheck</param-name>
			<param-value>true</param-value>
		</init-param>
		
		<!-- action 和 拦截器 所在的包，必要。多个路径用";"分开，目录会被递归扫描 -->
		<init-param>
			<param-name>actionPackages</param-name>
			<param-value>demo</param-value>
		</init-param>
		
		<!-- 上传文件的临时存放目录，需要上传功能时必要 -->
		<init-param>
			<param-name>uploadTemp</param-name>
			<param-value>D:/static/</param-value>
		</init-param>
		
		<!-- 自定义异常监听器，非必要。继承mint.mvc.core.ExceptionListener -->
		<init-param>
			<param-name>exceptionListener</param-name>
			<param-value>demo.CustomerExceptionListener</param-value>
		</init-param>
		
		<load-on-startup>0</load-on-startup>
		
		<!-- servlet3异步处理请求功能。需要上传功能时必要 -->
		<async-supported>true</async-supported>
	</servlet>
	
	<servlet-mapping>
		<servlet-name>dispatcher</servlet-name>
		<url-pattern>/</url-pattern>
	</servlet-mapping>
</web-app>
```
