package mint.mvc.core;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author LW
 * 组织内置参数的描素信息
 * 
 * 可以更具typeCode判断参数的类型,typeCode的存在是为了在参数初始化时判断更加快捷
 * 0:HttpServletRequest
 * 1:HttpServletResponse
 * 2:HttpSession
 * 3:Cookie[]
 * 4:Cookie
 */
public class BilidInArgumentInfo {
	public final int 		argIndex;
	public final String 	argName;
	public final Class<?>	argType;
	public final int		typeCode;

	/**
	 * HttpServletRequest 类型代号
	 */
	public final static int TYPE_REQUEST 	= 0;
	
	/**
	 * HttpServletResponse 类型代号
	 */
	public final static int TYPE_RESPONSE 	= 1;
	
	/**
	 * HttpSession 类型代号
	 */
	public final static int TYPE_SESSION 	= 2;
	
	/**
	 * Cookie[] 类型代号
	 */
	public final static int TYPE_COOKIES 	= 3;
	
	/**
	 * Cookie 类型代号
	 */
	public final static int TYPE_COOKIE 	= 4;
	
	BilidInArgumentInfo(int argIndex, String argName, Class<?> argType){
		this.argIndex 	= argIndex;
		this.argName 	= argName;
		this.argType	= argType;
		
		if (argType.equals(HttpServletRequest.class)) {
			typeCode = TYPE_REQUEST;
		} else if (argType.equals(HttpServletResponse.class)) {
			typeCode = TYPE_RESPONSE;
		} else if (argType.equals(HttpSession.class)) {
			typeCode = TYPE_SESSION;
		} else if (argType.equals(Cookie[].class)) {
			typeCode = TYPE_COOKIES;
		} else if (argType.equals(Cookie.class)){
			typeCode = TYPE_COOKIE;
		} else {
			typeCode = -1;
		}
	}
}
