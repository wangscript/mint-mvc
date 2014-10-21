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
 * Dispatcher handles ALL requests from clients, and dispatches to appropriate
 * handler to handle each request.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LW
 */
class Dispatcher {
	private final Logger logger = Logger.getLogger(this.getClass());

	private UrlMatcher[] getMatchers = null;
	private UrlMatcher[] postMatchers = null;
	private Map<UrlMatcher, ActionConfig> getUrlMap = new HashMap<UrlMatcher, ActionConfig>();
	private Map<UrlMatcher, ActionConfig> postUrlMap = new HashMap<UrlMatcher, ActionConfig>();

	public void init(Config config) throws ServletException {
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
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public Action dispatch(HttpServletRequest request) throws ServletException, IOException {
		String url 	= request.getRequestURI();
		String path = request.getContextPath();
		
		if (path.length() > 0) {
			url = url.substring(path.length());
		}
		
		// set default character encoding to "utf-8" if encoding is not set:
		if (request.getCharacterEncoding() == null) {
			request.setCharacterEncoding("UTF-8");
		}

		String 			reqMethod 		= request.getMethod().toUpperCase();
		ActionConfig 	actionConfig	= null;
		String[] 		urlArgs 		= null;
		
		/* 寻找处理请求的程序（方法） */
		if ("GET".endsWith(reqMethod)) {
			for (UrlMatcher m : this.getMatchers) {
				urlArgs = m.getUrlParameters(url);
				if (urlArgs != null) {
					actionConfig = getUrlMap.get(m);
					break;
				}
			}
		} else if ("POST".equals(reqMethod)) {
			for (UrlMatcher m : this.postMatchers) {
				urlArgs = m.getUrlParameters(url);
				if (urlArgs != null) {
					actionConfig = postUrlMap.get(m);
					break;
				}
			}
		}

		if(actionConfig != null){
			return new Action(actionConfig, urlArgs, url);
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
		logger.info("↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ start matching url ... ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓");
		ActionDetector ad = new ActionDetector();
		ad.awareActionMethodFromBeans(componentScaner.getActionBeans(config));
		getUrlMap.putAll(ad.getGetUrlMap());
		postUrlMap.putAll(ad.getPostUrlMap());
		logger.info("↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ end matching url ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑\n");

		/*
		 * TODO 检查相同的uri有没有匹配不同action 方法
		 */
		this.getMatchers = getUrlMap.keySet().toArray(new UrlMatcher[getUrlMap.size()]);
		this.postMatchers = postUrlMap.keySet().toArray(new UrlMatcher[postUrlMap.size()]);
	}

	public void destroy() {
		logger.info("Destroy Dispatcher...");
	}
}