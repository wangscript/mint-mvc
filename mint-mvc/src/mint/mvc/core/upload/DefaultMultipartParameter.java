package mint.mvc.core.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class DefaultMultipartParameter implements MultipartParameter{
	static Logger logger = Logger.getLogger(MultipartParameter.class.getName());
	
	boolean isFile 			= false;
	File 	tempFile 		= null;
	
	String	name			= null;
	String	filename		= null;
	String 	parameterValue 	= null;
	String	contentType		= null;
	
	Map<String, String> headers = new HashMap<String, String>();
	
	
	@Override
	public void delete() throws IOException {
		if(tempFile != null){
			if(tempFile.delete()){
				logger.warning("删除临时文件失败");
			};
		}
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public String getHeader(String headerName) {
		return headers.get(headerName);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return headers.keySet();
	}

	@Override
	public Collection<String> getHeaders(String headerName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if(tempFile != null){
			return new FileInputStream(tempFile);
		}
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getSize() {
		if(tempFile != null){
			return tempFile.length();
		}
		return 0;
	}

	@Override
	public void write(String fileName) throws IOException {
		if(isFile){
			File file = new File(fileName);
			if(!file.createNewFile()) return;
			
			//TODO
		}
	}

	@Override
	public boolean isFile() {
		return isFile;
	}

	@Override
	public String getParameterValue() {
		return parameterValue;
	}

	@Override
	public File getTempFile() {
		return tempFile;
	}

	@Override
	public String getFilename() {
		return filename;
	}
}
