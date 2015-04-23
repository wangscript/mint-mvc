package mint.mvc.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mint.mvc.converter.ConverterFactory;
import mint.mvc.core.annotation.InterceptorOrder;
import mint.mvc.core.annotation.MultipartConfig;
import mint.mvc.core.upload.FileUpload;
import mint.mvc.core.upload.MultipartHttpServletRequest;
import mint.mvc.core.upload.MultipartParameter;
import mint.mvc.renderer.Renderer;
import mint.mvc.renderer.TextRenderer;
import mint.mvc.template.JspTemplateFactory;
import mint.mvc.template.TemplateFactory;

import com.alibaba.fastjson.JSON;
import com.sun.istack.internal.logging.Logger;

/**
 * action的执行者。将请求传递过来的参数经过友好的封装，
 * 整理成action方法的参数，然后调用action方法，并且对
 * 方法的返回值做处理后返回
 *  
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午7:44:15 
 *
 */
class ActionExecutor {
	private final Logger logger = Logger.getLogger(this.getClass());
	
	private ServletContext servletContext;
	private ExceptionListener exceptionHandler;

	/**
	 * 拦截器
	 */
	private Interceptor[] interceptors = null;
	
	private ConverterFactory converterFactory = new ConverterFactory();
	
	private String uploadTemp;
	/**
	 * @param config
	 * @throws ServletException
	 */
	void init(Config config) throws ServletException {
		logger.info("Init Dispatcher...");
		this.servletContext = config.getServletContext();
		uploadTemp = config.getInitParameter("uploadTemp");
		
		String exHandler = config.getInitParameter("ExceptionHandler");
		if(exHandler!=null && !exHandler.equals("")){
			try {
				exceptionHandler = (ExceptionListener) Class.forName(exHandler).newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				logger.warning("can not init custom ExceptionHandler");
				e.printStackTrace();
			}
		} else {
			exceptionHandler = new DefaultExceptionListener();
		}
		
		try {
			initAll(config);
		} catch (Exception e) {
			throw new ServletException("Dispatcher init failed.", e);
		}
	}
	
	/**
	 * 调用action方法
	 * @param actionConfig
	 * @param arguments
	 * @return
	 * @throws Exception
	 */
	Object executeActionMethod(ActionConfig actionConfig, Object[] arguments) throws Exception {
		try {
			return actionConfig.actionMethod.invoke(actionConfig.instance, arguments);
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			if (t != null && t instanceof Exception){
				throw (Exception) t;
			}
			throw e;
		}
	}
	
	void executeAction(HttpServletRequest request, HttpServletResponse response, Action action) throws ServletException, IOException{
		ActionContext.setActionContext(servletContext, request, response);
		
		/* 拦截器链 */
		InterceptorChainImpl chain = new InterceptorChainImpl(interceptors, action.uri);
		try {
			chain.doInterceptor(ActionContext.getActionContext());
		} catch (Exception e) {
			ActionContext.removeActionContext();
			handleException(request, response, e);
		}
		
		if (chain.isPass()) {
			/*处理上传请求*/
			String contentType = request.getContentType();
			if(contentType != null && contentType.indexOf("multipart/form-data") >= 0){
				/*先用异步阻塞上传过程，避免“上传任意文件”*/
				AsyncContext acontext = request.startAsync();
				acontext.setTimeout(0);
				
				if(action.actionConfig.isMultipartAction){
					MultipartConfig multipartConfig = action.actionConfig.multipartConfig;
					if(multipartConfig.maxRequestSize() <= 0 || (request.getContentLength() < multipartConfig.maxRequestSize())){
						final Object lock = new Object();
						boolean uploading = FileUpload.upload(uploadTemp, multipartConfig.attributeName(), multipartConfig.limitSize(), acontext, lock);
						
						/*当文件正在上传时，阻塞当前线程，待文件上传完毕时，再唤醒当前线程，达到同步效果。唤醒当前线程的代码在文件上传线程中*/
						if(uploading){
							synchronized (lock) {
								try {
									lock.wait();
								} catch (InterruptedException e) {
									handleException(request, response, e);
								}
							}
							
							if(request.getAttribute(multipartConfig.attributeName()) != null){
								MultipartHttpServletRequest r = new MultipartHttpServletRequest(request, (MultipartParameter[]) request.getAttribute(multipartConfig.attributeName()));
								request = r;
							}
						}
					} else {
						logger.warning("request body is too large");
						acontext.complete();
					}
				} else {
					acontext.complete();
				}
			}
			
			/*调用action方法*/
			Object[] arguments = initArguments(request, response, action);
			try {
				handleResult(request, response, executeActionMethod(action.actionConfig, arguments), action.actionConfig);
			} catch (Exception e) {
				ActionContext.removeActionContext();
				handleException(request, response, e);
			}
		}
	}
	
	/**
	 * @param config
	 */
	void initAll(Config config){
		initInterceptors(config);
		initTemplateFactory(config);
	}
	
	/**
	 * 初始化拦截器
	 * @param config
	 */
	void initInterceptors(Config config){
		ComponentScaner componentScaner = new ComponentScaner();
		Set<Interceptor> itcps = componentScaner.getInteceptorBeans(config);
		
		/*
		 * 拦截器 拦截器排序
		 */
		this.interceptors = itcps.toArray(new Interceptor[itcps.size()]);
		
		Arrays.sort(this.interceptors, new Comparator<Interceptor>() {
			public int compare(Interceptor i1, Interceptor i2) {
				InterceptorOrder o1 = i1.getClass().getAnnotation(InterceptorOrder.class);
				InterceptorOrder o2 = i2.getClass().getAnnotation(InterceptorOrder.class);
				int n1 = o1 == null ? Integer.MAX_VALUE : o1.value();
				int n2 = o2 == null ? Integer.MAX_VALUE : o2.value();
				if (n1 == n2) {
					return i1.getClass().getName().compareTo(i2.getClass().getName());
				}
				return n1 < n2 ? (-1) : 1;
			}
		});
	}
	
	/**
	 * @param config
	 */
	private void initTemplateFactory(Config config) {
		String name = config.getInitParameter("template");
		if (name == null) {
			name = JspTemplateFactory.class.getName();
			logger.info("No template factory specified. Default to '" + name + "'.");
		}
		Utils util = new Utils();
		TemplateFactory tf = util.createTemplateFactory(name);
		tf.init(config);
		logger.info("Template factory '" + tf.getClass().getName() + "' init ok.");
		TemplateFactory.setTemplateFactory(tf);
	}
	
	/**
	 * prepare arguments for action method by parameter in request.
	 * 
	 * @param req
	 * @param action
	 * @param matcher
	 */
	private Object[] initArguments(HttpServletRequest req, HttpServletResponse resp, Action action) {
		ActionConfig actionConfig = action.actionConfig;
		
		Object[] arguments = new Object[actionConfig.argumentTypes.length];

		/* 从url获取参数（parameter）初始化action 方法参数（argument） */
		String[] urlArgs = action.urlParams;
		
		int argIndex;
		int[] urlArgOrder = actionConfig.urlArgumentOrder; // 对应位置的参数接受从url中分离出来的参数
		
		String str;
		for (int i = 0; i < urlArgs.length; i++) {
			argIndex = urlArgOrder[i];
			
			str = urlArgs[i];
			
			/*
			 * 如果参数中有“%”，说明该参数被编码过，需要解码。目前只支持utf8编码的解码
			 */
			if(str.contains("%")){
				try {
					str = URLDecoder.decode(urlArgs[i],  "utf8");
				} catch (UnsupportedEncodingException e) { }
			}
			
			arguments[argIndex] = converterFactory.convert(actionConfig.argumentTypes[argIndex], str);
		}

		/* 从请求参数中初始化action方法参数(argument) */
		Map<String, String[]> paramMap = req.getParameterMap();
		Object arguInstance;
		Map<String, ParameterInjector> injectors = actionConfig.injectors;
		ParameterInjector injector;
		
		try {
			for (String paramName : paramMap.keySet()) {
				injector = injectors.get(paramName);
				
				if (injector != null) {
					if (injector.needInject) {
						arguInstance = arguments[injector.argIndex];
						if (arguInstance == null) {
							/* instantiate a instance the first time you use */
							arguInstance = injector.argType.newInstance();
							arguments[injector.argIndex] = arguInstance;
						}
						
						arguments[injector.argIndex] = injector.inject(arguInstance, paramMap.get(paramName)[0], paramName);
					} else {
						if (injector.isArray) {
							/*
							 * 支持数组
							 */
							String array[] = paramMap.get(paramName);
							int len = array.length;
							Class<?> t = injector.argType.getComponentType();
							Object arr = Array.newInstance(t, len);
							for (int i = 0; i < len; i++) {
								Array.set(arr, i, converterFactory.convert(t, array[i]));
							}
	
							arguments[injector.argIndex] = arr;
						} else {
							/* 简单类型直接转换 */
							arguments[injector.argIndex] = converterFactory.convert(injector.argType, paramMap.get(paramName)[0]);
						}
					}
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} 
		
		/*
		 * 从request.getAttributeNames()初始化参数
		 */
		Enumeration<String> attributes = req.getAttributeNames();
		Object attribute;
		String attributeName;
		injector = null;
		injectors = actionConfig.injectors;
		while(attributes.hasMoreElements()){
			attributeName = attributes.nextElement();
			attribute = req.getAttribute(attributeName);
			
			if(attribute != null){
				injector = injectors.get(attributeName);
				/*attributeName and attributeType 匹配时，进行参数替换*/
				if(injector != null && injector.argType.isInstance(attribute)){
					arguments[injectors.get(attributeName).argIndex] = attribute;
				}
			}
		}
		
		/* 初始化内置参数 */
		if (actionConfig.builtInArguments != null) {
			for (BilidInArgumentInfo info : actionConfig.builtInArguments) {
				switch (info.typeCode) {
					case 0:{
						arguments[info.argIndex] = req;
						break;
					}
					case 1:{
						arguments[info.argIndex] = resp;
						break;
					}	
					case 2:{
						arguments[info.argIndex] = req.getSession();
						break;
					}
					case 3:{
						arguments[info.argIndex] = req.getCookies();
						break;
					}
					case 4:{
						Cookie[] cookies = req.getCookies();
						
						if(cookies!=null){
							for(Cookie cookie : cookies){
								if(cookie.getName().equals(info.argName)){
									arguments[info.argIndex] = cookie;
									break;
								}
							}
						}
						break;
					}
					
					default:
						break;
				}
			}
		}

		return arguments;
	}

	/* 当方法出现异常时，处理异常 */
	private void handleResult(HttpServletRequest request, HttpServletResponse response, Object result, ActionConfig actionConfig) throws Exception {
		if (result == null) {
			return;
		}
		
		/*处理模板结果*/
		if (result instanceof Renderer) {
			((Renderer) result).render(this.servletContext, request, response);
			return;
		}
		
		/*处理json结果*/
		if(actionConfig.returnJson){
			new TextRenderer(JSON.toJSONString(result)).render(servletContext, request, response);
			return;
		} else {
			new TextRenderer(result+"").render(servletContext, request, response);
			return;
		}
	}
	
	private void handleException(HttpServletRequest request, HttpServletResponse response, Exception ex) throws ServletException, IOException {
		try {
			exceptionHandler.handle(request, response, ex);
		} catch (ServletException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
