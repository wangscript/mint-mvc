package mint.mvc.core;

/**
 * 
 * @Description: action的配置信息
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午7:43:05 
 *
 */
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
