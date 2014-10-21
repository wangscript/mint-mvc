package mint.mvc.core.upload;

import java.io.File;

import javax.servlet.http.Part;

public interface  MultipartParameter extends Part{
	/**
	 * 是否文件
	 * @return
	 */
	public boolean isFile();
	
	/**
	 * 获取非文件参数值
	 * @return
	 */
	public String getParameterValue();
	
	/**
	 * 获取临时文件
	 * @return
	 */
	public File getTempFile();
	
	/**
	 * 获取客户端上传文件的文件名
	 * @return
	 */
	public String getFilename();
}
