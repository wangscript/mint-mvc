package demo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mint.mvc.core.ExceptionListener;

public class CustomerExceptionListener implements ExceptionListener{

	/* 自定义异常处理器
	 */
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, Exception e) throws Exception {
		System.out.println("server serious exception has occurred,check emali for detail.");
		e.printStackTrace();
	}

}
