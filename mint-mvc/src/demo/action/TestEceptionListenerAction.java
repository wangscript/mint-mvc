package demo.action;

import mint.mvc.core.annotation.BaseMapping;
import mint.mvc.core.annotation.Mapping;

@BaseMapping("/exception")
public class TestEceptionListenerAction {
	
	/**
	 * ExceptionListener起作用
	 * @throws Exception
	 */
	@Mapping(urls="", method="get")
	public void problem() throws Exception{
		throw new Exception();
	}
}
