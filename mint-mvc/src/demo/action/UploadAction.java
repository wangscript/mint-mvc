package demo.action;

import mint.mvc.core.annotation.BaseMapping;
import mint.mvc.core.annotation.Mapping;
import mint.mvc.core.annotation.MultipartConfig;
import mint.mvc.core.upload.MultipartParameter;


@BaseMapping("/upload")
public class UploadAction {
	/**
	 * 如果使用文件上传功能，需要给全段控制器指定一个启动参数，
	 * 参数的值就是文件上传的临时存放目录，如下：
	 * 
	 * <init-param>
		<param-name>uploadTemp</param-name>
		<param-value>/static/</param-value>
	</init-param>
	 * 
	 * 当文件上传完毕之后，request对象中会有一个名为files的"attribute"。request中的
	 * attribute可以根据名字注入到方法参数中
	 * 
	 * limitsize规定了每个文件（或者表单域）的最大长度
	 * 
	 * 文件将会上传到web.xml配置文件中指定的临时目录
	 */
	@MultipartConfig(attributeName = "files", limitSize = 1024*1024*1024)
	@Mapping(urls="/index", method="post")
	public String index(MultipartParameter[] files, String author){
		String fileParam = "";
		String commonParam = "";
		
		if(files != null){
			for(MultipartParameter part : files){
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