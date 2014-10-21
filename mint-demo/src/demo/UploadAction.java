package demo;

import mint.mvc.core.annotation.BaseMapping;
import mint.mvc.core.annotation.Mapping;
import mint.mvc.core.upload.MultipartConfig;
import mint.mvc.core.upload.MultipartParameter;

@BaseMapping("/upload")
public class UploadAction {
	@MultipartConfig(attributeName = "params", limitSize = 1024*1024*1024, tempFilePath = "D:/upload")
	@Mapping(urls="/index", method="post")
	public String index(MultipartParameter[] params, String author){
		String fileParam = "";
		String commonParam = "";
		
		if(params != null){
			for(MultipartParameter part : params){
				if(part.isFile()){
					fileParam += part.getTempFile().getAbsolutePath()+"<br/>";
				} else {
					commonParam += (part.getName()+":"+part.getParameterValue())+"<br/>";
				}
			}
		}
		
		System.out.println(author);
		
		return fileParam+commonParam;
	}
}