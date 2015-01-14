package demo;

import javax.servlet.http.HttpServletRequest;

import mint.mvc.core.annotation.BaseMapping;
import mint.mvc.core.annotation.Mapping;
import mint.mvc.core.annotation.MultipartConfig;
import mint.mvc.core.upload.MultipartParameter;

@BaseMapping("/upload")
public class UploadAction {
	
	@MultipartConfig(attributeName = "files", limitSize = 1024*1024*1024)
	@Mapping(urls={"/index", "/index/{id}"}, method="post")
	public String index(Integer id, String name, String username, MultipartParameter[] files, HttpServletRequest request){
		return "index";
	}
}