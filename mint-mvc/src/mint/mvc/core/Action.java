package mint.mvc.core;

public class Action {
	ActionConfig 	actionConfig;
	String[] 		urlParams;
	String 			uri;
	
	public Action(ActionConfig actionConfig, String[] urlParams, String uri){
		this.actionConfig 	= actionConfig;
		this.urlParams 		= urlParams;
		this.uri			= uri;
	}
}
