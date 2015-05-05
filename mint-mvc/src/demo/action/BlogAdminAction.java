package demo.action;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import mint.mvc.annotation.BaseMapping;
import mint.mvc.annotation.Mapping;
import mint.mvc.annotation.ReturnJson;
import mint.mvc.renderer.FileRenderer;
import mint.mvc.renderer.TemplateRenderer;
import demo.Blog;

@BaseMapping("/blogAdmin")
public class BlogAdminAction {
	
	/**
	 * 返回基础类型和普通类，demo 1
	 * @param id
	 * @return
	 */
	@Mapping(urls="/isExist/{id}", method="get")
	public boolean isExist(Long id){
		return id.equals(1024L);
	}
	
	/**
	 * 返回基础类型和普通类，demo 2
	 * @param id
	 * @return
	 */
	@Mapping(urls="/{id:\\\\d+}", method="get")
	public Blog getBlog(Long id){
		Blog blog = new Blog();
		blog.setId(id);
		
		return blog;
	}
	
	/**
	 * 返回JSON
	 * @param id
	 * @return
	 */
	@ReturnJson
	@Mapping(urls="/json/{id}", method="get")
	public Blog getBlog1(Long id){
		Blog blog = new Blog();
		blog.setId(id);
		blog.setTitle("mint-mvc");
		blog.setTitle("返回值");
		
		return blog;
	}
	
	/**
	 * 返回渲染器(内置渲染器FileRenderer)
	 * @param fileName
	 * @param request
	 * @return
	 */
	@Mapping(urls="/static/{fileName}", method="get")
	public FileRenderer staticFile(String fileName, HttpServletRequest request){
		String cssPath = request.getServletContext().getRealPath("/css");
		
		return new FileRenderer(cssPath+File.separator+fileName);
	}
	
	/**
	 * 用TemplateRenderer返回jsp模板
	 * @param request
	 * @return
	 */
	@Mapping(urls="/article")
	public TemplateRenderer blog(HttpServletRequest request){
		Blog blog = new Blog();
		blog.setId(1024L);
		blog.setTitle("mint-mvc");
		blog.setContent("用TemplateRenderer返回jsp模板");
		
		//request.setAttribute("blog", blog);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("blog", blog);
		return new TemplateRenderer("/blog.jsp", param);
	}
	
	/**
	 * 自定义渲染器返回json格式数据
	 * @return
	 */
	@Mapping(urls="/jsonrenderer", method="get")
	public JSONRenderer testRenderer(){
		Blog blog = new Blog();
		blog.setTitle("自定义渲染器");
		blog.setContent("灵活方便，自定义数据的返回形式");
		return new JSONRenderer(blog);
	}
}