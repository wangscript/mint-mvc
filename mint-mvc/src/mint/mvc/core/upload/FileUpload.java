package mint.mvc.core.upload;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;

import com.sun.istack.internal.logging.Logger;

/**
 * @author LW
 * 文件上传的工具类
 */
public class FileUpload {
	private static Logger logger = Logger.getLogger(FileUpload.class);
	private HttpServletRequest request;
	
	public FileUpload(HttpServletRequest request){
		this.request = request;
	}
	
	public void upload(String tempFilePath, String attributeName, long limitSize, AsyncListener listener){
		AsyncContext acontext = request.getAsyncContext();
		acontext.setTimeout(0);
		
		upload(tempFilePath, attributeName, limitSize, null);
	}
	
	/**
	 * @param tempFilePath 文件上传的临时保存路径
	 * @param attributeName 表单处理完毕后，将参数添加到request时用到的属性名，action内可以通过request.getAttribute()方法获取
	 * @param limitSize 表单项的最大长度，小于零表示无限制
	 * @param lock
	 * @return
	 */
	public static boolean upload(String tempFilePath, String attributeName, long limitSize, AsyncContext acontext, Object lock){
		if(attributeName == null || "".equals(attributeName)) {
			logger.warning("请指定 attributeName");
			return false;
		}
		
		if(tempFilePath == null || "".equals(tempFilePath)) {
			logger.warning("请指定 tempFilePath");
			return false;
		}
		try{
			acontext.start(new UploadExecutor(acontext, tempFilePath, attributeName, limitSize, lock));
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
