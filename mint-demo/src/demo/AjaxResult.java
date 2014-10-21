package demo;

public class AjaxResult {
	private boolean result;
	private String resultMsg;
	private Object data;
	
	public boolean isResult() {
		return result;
	}
	public AjaxResult setResult(boolean result) {
		this.result = result;
		return this;
	}
	public Object getData() {
		return data;
	}
	public AjaxResult setData(Object data) {
		this.data = data;
		return this;
	}
	public String getResultMsg() {
		return resultMsg;
	}
	public AjaxResult setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
		return this;
	}
}
