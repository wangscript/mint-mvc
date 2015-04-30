package demo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mint.mvc.core.ExceptionListener;

public class CustomerExceptionListener implements ExceptionListener{

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, Exception e) throws Exception {
		System.out.println("hi，firends");
	}

}
