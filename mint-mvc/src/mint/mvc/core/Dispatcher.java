package mint.mvc.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import mint.mvc.core.Action;
import mint.mvc.core.ActionDetector;
import mint.mvc.core.ComponentScaner;
import mint.mvc.core.Config;

import com.sun.istack.internal.logging.Logger;


/**
 * @Description: Dispatcher handles ALL requests from clients, and dispatches to appropriate
 * handler to handle each request.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:07:16 
 *
 */
class Dispatcher {
	private final Logger logger = Logger.getLogger(this.getClass());

	private Map<String, Map<UrlMatcher, ActionConfig>> urlMapMap = new HashMap<String, Map<UrlMatcher, ActionConfig>>();
	private Map<String, UrlMatcher[]> matchersMap = new HashMap<String, UrlMatcher[]>();
	
	void init(Config config) throws ServletException {
		logger.info("Init Dispatcher...");
		try {
			initAll(config);
		} catch (ServletException e) {
			throw e;
		} catch (Exception e) {
			throw new ServletException("Dispatcher init failed.", e);
		}
	}

	/**
	 * http://example.com:8080/over/there?name=ferret#nose
	 * \__/   \______________/\_________/ \_________/ \__/
	 *   |         |                |         |         |
	 * scheme   authority          path     query    fragment
	 *
	 * OR
	 * 
	 * [scheme:][//authority][path][?query][#fragment]
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	Action dispatch(HttpServletRequest request) throws ServletException, IOException {
		String path = request.getRequestURI();
		String ctxP = request.getContextPath();
		
		if (ctxP.length() > 0) {
			path = path.substring(ctxP.length());
		}
		
		// set default character encoding to "utf-8" if encoding is not set:
		if (request.getCharacterEncoding() == null) {
			request.setCharacterEncoding("UTF-8");
		}

		String 			reqMethod 		= request.getMethod().toUpperCase();
		ActionConfig 	actionConfig	= null;
		String[] 		urlArgs 		= null;
		
		/* 寻找处理请求的程序（方法） */
		for (UrlMatcher m : this.matchersMap.get(reqMethod)) {
			urlArgs = m.getUrlParameters(path);
			if (urlArgs != null) {
				actionConfig = urlMapMap.get(reqMethod).get(m);
				break;
			}
		}
		
		if(actionConfig != null){
			return new Action(actionConfig, urlArgs, path);
		}
		
		return null;
	}

	/**
	 * 
	 * @param config
	 * @throws Exception
	 */
	private void initAll(Config config) throws Exception {
		initActions(config);
	}

	/* 初始化action */
	private void initActions(Config config) {
		ComponentScaner componentScaner = new ComponentScaner();

		/* 初始化action */
		logger.info("\n");
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start matching url ... <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		ActionDetector ad = new ActionDetector();
		ad.awareActionMethodFromBeans(componentScaner.getActionBeans(config));
		
		this.urlMapMap.put("GET", ad.getUrlMap);
		this.urlMapMap.put("POST", ad.postUrlMap);
		this.urlMapMap.put("PUT", ad.putUrlMap);
		this.urlMapMap.put("DELETE", ad.deleteUrlMap);
		
		/*
		 * TODO 检查相同的uri有没有匹配不同action 方法
		 */
		this.matchersMap.put("GET", ad.getUrlMap.keySet().toArray(new UrlMatcher[ad.getUrlMap.size()]));
		this.matchersMap.put("POST", ad.postUrlMap.keySet().toArray(new UrlMatcher[ad.postUrlMap.size()]));
		this.matchersMap.put("PUT", ad.putUrlMap.keySet().toArray(new UrlMatcher[ad.putUrlMap.size()]));
		this.matchersMap.put("DELETE", ad.deleteUrlMap.keySet().toArray(new UrlMatcher[ad.deleteUrlMap.size()]));
		
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> end matching url <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
	}

	void destroy() {
		logger.info("Destroy Dispatcher...");
	}
}