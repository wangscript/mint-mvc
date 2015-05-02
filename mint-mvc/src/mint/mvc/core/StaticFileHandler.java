package mint.mvc.core;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mint.mvc.renderer.FileRenderer;

/**
 * 
 * 
 * 
 */

/**
 * 
 * Handle static file request.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:12:08 
 *
 */
class StaticFileHandler {
	private final ServletContext servletContext;
	private final String cacheControl;
	private final Boolean lastModifiedCheck;
	private final String contextPath;
	
	//private final String webRoot;

	/**
	 * @param config
	 * @throws ServletException
	 */
	StaticFileHandler(ServletConfig config) throws ServletException {
		this.servletContext = config.getServletContext();
		this.contextPath = config.getServletContext().getContextPath();
		
		String 	cc = config.getInitParameter("staticFileCacheControl"),
				lmfc = config.getInitParameter("staticFileLastModifiedCheck");
		
		if(cc != null){
			cacheControl = cc;
		} else {
			cacheControl = "max-age=600";
		}
		
		if(lmfc != null){
			lastModifiedCheck = Boolean.parseBoolean(lmfc);
		} else {
			lastModifiedCheck = true;
		}
	}

	/**
	 * 响应静态文件请求
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = request.getRequestURI();
		url = url.substring(contextPath.length());
		
		if (url.toUpperCase().startsWith("/WEB-INF/")) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		/*
		 * TODO
		 * 自定义目录的访问控制
		 */
		
		int n = url.indexOf('?');
		if (n!=(-1)){
			url = url.substring(0, n);
		}
		
		n = url.indexOf('#');
		if (n!=(-1)){
			url = url.substring(0, n);
		}
		
		File f = new File(servletContext.getRealPath(url));
		if (! f.isFile()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		FileRenderer fr = new FileRenderer(f);
		fr.setCacheControl(cacheControl);
		fr.setLastModifiedCheck(lastModifiedCheck);
		fr.setConnection("keep-alive");
		
		try {
			fr.render(servletContext, request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
