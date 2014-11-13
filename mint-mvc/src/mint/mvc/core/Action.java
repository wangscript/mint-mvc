package mint.mvc.core;

class Action {
	ActionConfig 	actionConfig;
	String[] 		urlParams;
	String 			uri;
	
	Action(ActionConfig actionConfig, String[] urlParams, String uri){
		this.actionConfig 	= actionConfig;
		this.urlParams 		= urlParams;
		this.uri			= uri;
	}
}
