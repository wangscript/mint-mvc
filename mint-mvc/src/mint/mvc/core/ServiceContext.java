package mint.mvc.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author LW
 * 服务上下文
 */
public class ServiceContext {
	final HttpServletRequest req; 
	final HttpServletResponse resp;
	
	ServiceContext(HttpServletRequest req, HttpServletResponse resp){
		this.req = req;
		this.resp = resp;
	}
}
