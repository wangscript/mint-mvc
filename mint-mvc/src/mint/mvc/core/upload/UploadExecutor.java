package mint.mvc.core.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.AsyncContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

final class UploadExecutor implements Runnable{
	private static Logger logger = Logger.getLogger(UploadExecutor.class.getName());
	
	private AsyncContext 	context;
	private String 			tempFilePath;
	private String 			attributeName;
	private Object 			lock;
	private long  			limitSize;
	
	UploadExecutor(AsyncContext context, String tempFilePath, String attributeName, long limitSize, Object lock){
		this.context 		= context;
		this.tempFilePath 	= tempFilePath;
		this.attributeName 	= attributeName;
		this.limitSize		= limitSize>0 ? limitSize : Long.MAX_VALUE;
		this.lock 			= lock;
	}
	
	public void run() {
		//为了不让猛兽逃出来，以至于原来请求线程一直锁死，在此把它抓住
		try{
			HttpServletRequest request = (HttpServletRequest) context.getRequest();
			/*解析请求体*/
			parseRequestBody(request);
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			/*文件上传完毕，执行原来的线程*/
			if(lock != null){
				synchronized (lock) {
					lock.notify();
				}
			}
			context.complete();
		}
	}
	
	/**
	 * 本方法效率优先
	 * @param request
	 */
	void parseRequestBody(HttpServletRequest request){
		ServletInputStream inputStream = null;
		try {
			inputStream = request.getInputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		
		String boundary = null;
		//获取请求体的分隔符
		for(String s : request.getHeader("Content-Type").split(";")){
			if(s.indexOf("boundary=") > 0){
				boundary = "--"+s.split("=")[1];
			}
		}
		
		List<MultipartParameter> multiParam = null;
		FileOutputStream fileOut = null;
		try {
			byte[] readBuf = new byte[1024*4];
			//byte[] writeBuf = new byte[1024*16];
			//int writeLen = 0;
			int readLen = 0;
			
			//用到的所有局部变量，为了效率，所有不在循环体内声明
			DefaultMultipartParameter currentPart = null;
			File tempFile;
			boolean isFile = false;
			
			boolean end = false;
			String line, partInfo, mimeType = null;
			StringBuffer paramValue = new StringBuffer(512);
			
			/*缓存分隔符及其长度，提高性能*/
			byte[] boundaryByte = boundary.getBytes();
			int startBoundaryLen = boundaryByte.length+2;  	//回车 和 换行 符（\r\n）
			int endBoundaryLen = startBoundaryLen + 2; 		//--
			
			readLen = inputStream.readLine(readBuf, 0, readBuf.length);
			line = new String(readBuf, 0, readLen);
			
			//第一行必须是分隔符
			if(!line.startsWith(boundary)){
				logger.warning("文件上传数据格式不正确");
				return;
			}
			
			if(line.startsWith(boundary+"--")){return;}
			
			multiParam = new ArrayList<MultipartParameter>();
			long partSize = 0;
			
			while(!end){
				if(!end && readLen < 0){
					logger.warning("非法请求：不是标准的文件上传请求");
					return;
				}
				
				//分析头部，分析分隔符
				//还未读取到请参数结尾
				
				line = new String(readBuf, 0, readLen);
				if(!line.startsWith(boundary+"--")){
					currentPart = new DefaultMultipartParameter();

					partInfo = new String(readBuf, 0, inputStream.readLine(readBuf, 0, readBuf.length));
					Map<String, String> info = parsePartInfo(partInfo, null);
					
					if(info.get("name") == null || "".equals(info.get("name"))){
						logger.warning("非法请求：包含无名参数");
						break;
					}
					
					currentPart.name = info.get("name");
					
					/*文件头部*/
					if(info.get("filename") != null){
						currentPart.isFile = true;
						
						String fileName = info.get("filename");
						
						/*为了解决IE上传文件时文件名为绝对路径的情况*/
						if(fileName.lastIndexOf("\\") > 0){
							currentPart.filename = fileName.substring(fileName.lastIndexOf("\\")+1);
						} else {
							currentPart.filename = fileName;
						}
						
						partInfo =  new String(readBuf, 0, inputStream.readLine(readBuf, 0, readBuf.length));
						
						/*文件mimetype*/
						if(partInfo.startsWith("Content-Type: ")){
							mimeType = partInfo.split(":")[1].trim();
							
							if("".equals(mimeType)){
								logger.warning("非法请求：文件参数描述信息不全");
								break;
							}
							
							currentPart.contentType = mimeType;
							
							isFile = true;
							tempFile = createTempFile(tempFilePath, currentPart.filename);
							fileOut = new FileOutputStream(tempFile);
							currentPart.tempFile = tempFile;
						} else {
							logger.warning("非法请求：文件参数描述信息不全");
							break;
						}
					} else {
						isFile = false;
					}
				} else {
					end = true;
				}
				
				//跳过描述头和内容之间的换行符
				inputStream.readLine(readBuf, 0, 3);
				
				/*
				 * 为了性能，以下循环尽量不生成垃圾变量
				 * 逻辑较强，可读性较差
				 */
				if(isFile){
					//解析文件内容
					int j, i;
					while((readLen = inputStream.readLine(readBuf, 0, readBuf.length)) > 0){
						//有可能出现分隔符
						if(readLen == startBoundaryLen || readLen == endBoundaryLen){
							i = 0; j=0;
							
							//不需要比较后面的回车换行符了
							for(; i<startBoundaryLen-2; i++){
								if(boundaryByte[i] != readBuf[i]) {
									j = 1;
									break;
								}
							}
							
							//当前part分析完成，分析下一个头部
							if(j == 0){
								//fileOut.write(writeBuf, 0, writeLen);
								//writeLen = 0;
								partSize = 0;
								multiParam.add(currentPart);
								
								fileOut.flush();
								fileOut.close();
								fileOut = null;
								break;
							}
						}
						
						partSize += readLen;
						
						if(partSize <= limitSize){
							fileOut.write(readBuf, 0, readLen);
							fileOut.flush();
						} else {
							logger.warning("上传文件超过期望长度");
							end = true;
							break;
						}
						
						//先缓存再写入。在一些磁盘io资源不足的应用要开启文件缓冲功能
						/*for(i=0; i<readLen; i++){
							writeBuf[writeLen] = readBuf[i];	
							writeLen += 1;
							
							if(writeLen == writeBuf.length){
								fileOut.write(writeBuf, 0, writeLen);
								writeLen = 0;
							}
						}*/
					}
				} else {
					//解析普通内容
					int j, i;
					paramValue.delete(0, paramValue.length());
					while((readLen = inputStream.readLine(readBuf, 0, readBuf.length)) > 0){
						//有可能出现分隔符
						if(readLen == startBoundaryLen || readLen == endBoundaryLen){
							i = 0; j=0;
							
							//不需要比较后面的回车换行符了
							for(; i<startBoundaryLen-2; i++){
								if(boundaryByte[i] != readBuf[i]) {
									j = 1;
									break;
								}
							}
							
							//当前part分析完成，分析下一个头部
							if(j == 0){
								partSize = 0;
								currentPart.parameterValue = paramValue.toString().trim();
								multiParam.add(currentPart);
								break;
							}
						}
						
						partSize += readLen;
						
						if(partSize <= limitSize){
							paramValue.append(new String(readBuf, 0, readLen, "utf8"));
						} else {
							logger.warning("请求参数超过期望长度");
							end = true;
							break;
						}
					}
				}
			}
			inputStream.close();
		} catch (IOException e) {
			logger.warning("文件上传失败");
		} finally{
			try {
				if(fileOut != null) fileOut.close();
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(multiParam != null && multiParam.size() > 0){
			DefaultMultipartParameter[] ps = new DefaultMultipartParameter[multiParam.size()];
			
			for(int i=0; i<ps.length; i++){
				ps[i] = (DefaultMultipartParameter) multiParam.get(i);
			}
			
			request.setAttribute(attributeName, ps);
		}
	}
	
	/**
	 * 形如:
	 * Content-Disposition: form-data; name="file"; filename="test上传.txt"
	 * 被解析成map结构返回
	 * @param info 描述信息
	 * @param split key[split]value 的分隔符，默认是";"
	 * @return
	 */
	Map<String, String>parsePartInfo(String info, String split){
		if(info == null || "".equals(info.trim())) return null;
		
		Map<String, String> partInfos = new HashMap<String, String>();
		
		if(split == null) split = "=";
		for(String s : info.split(";")){
			String kv[] = s.split(split);
			if(kv.length == 2){
				partInfos.put(kv[0].trim(), kv[1].replace("\"", "").trim());
			}
		}
		
		return partInfos;
	}
	
	/**
	 * @param basepath
	 * @return
	 * @throws IOException 
	 */
	File createTempFile(String basepath, String filename) throws IOException{
		File tempFile = new File(basepath, UUID.randomUUID().toString().replace("-", "")+"_"+filename);
		while(tempFile.exists()){
			tempFile = new File(basepath, UUID.randomUUID().toString().replace("-", "")+"_"+filename);
		}
		try {
			tempFile.createNewFile();
			return tempFile;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("无法创建临时文件");
		}
	}
}