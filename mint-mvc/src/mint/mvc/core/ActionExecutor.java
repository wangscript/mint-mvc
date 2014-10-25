package mint.mvc.core;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
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
import javax.servlet.http.HttpSession;

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
 * @author LW
 * action的执行者。将请求传递过来的参数经过友好的封装，
 * 整理成action方法的参数，然后调用action方法，并且对
 * 方法的返回值做处理后返回
 */
class ActionExecutor {
	private final Logger logger = Logger.getLogger(this.getClass());
	
	private ServletContext servletContext;
	private ExceptionHandler exceptionHandler = new DefaultExceptionHandler();

	/**
	 * 拦截器
	 */
	private Interceptor[] interceptors = null;
	
	private ConverterFactory converterFactory = new ConverterFactory();
	
	/**
	 * @param config
	 * @throws ServletException
	 */
	void init(Config config) throws ServletException {
		logger.info("Init Dispatcher...");
		this.servletContext = config.getServletContext();
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
					final Object lock = new Object();
					boolean uploading = FileUpload.upload(multipartConfig.tempFilePath(), multipartConfig.attributeName(), multipartConfig.limitSize(), acontext, lock);
					
					/*当文件正在上传时，阻塞当前线程，待文件上传完毕时，再唤醒当前线程，达到同步效果。唤醒当前线程的代码在文件上传线程中*/
					if(uploading){
						synchronized (lock) {
							try {
								lock.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						
						if(request.getAttribute(multipartConfig.attributeName()) != null){
							MultipartHttpServletRequest r = new MultipartHttpServletRequest(request, (MultipartParameter[]) request.getAttribute(multipartConfig.attributeName()));
							request = r;
						}
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
		TemplateFactory tf = Utils.createTemplateFactory(name);
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
		for (int i = 0; i < urlArgs.length; i++) {
			argIndex = urlArgOrder[i];
			arguments[argIndex] = converterFactory.convert(actionConfig.argumentTypes[argIndex], urlArgs[i]);
		}

		/* 从请求参数中初始化action方法参数(argument) */
		Map<String, String[]> paramMap = req.getParameterMap();
		Object instance;
		Map<String, ParameterInjector> injectors = actionConfig.injectors;
		ParameterInjector injector;

		for (String paramName : paramMap.keySet()) {
			injector = injectors.get(paramName);
			if (injector != null) {
				if (injector.needInject) {
					instance = arguments[injector.argumentIndex];
					if (instance == null) {
						try {
							/* instantiate a instance the first time you use */
							instance = injector.argumentType.newInstance();
							arguments[injector.argumentIndex] = instance;
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}

					instance = arguments[injector.argumentIndex] = injector.inject(instance, paramMap.get(paramName)[0], paramName);
				} else {
					if (injector.isArray) {
						/*
						 * 支持数组
						 */
						String array[] = paramMap.get(paramName);
						int len = array.length;
						Class<?> t = injector.argumentType.getComponentType();
						Object arr = Array.newInstance(t, len);
						for (int i = 0; i < len; i++) {
							Array.set(arr, i, converterFactory.convert(t, array[i]));
						}

						arguments[injector.argumentIndex] = arr;
					} else {
						/* 简单类型直接转换 */
						arguments[injector.argumentIndex] = converterFactory.convert(injector.argumentType, paramMap.get(paramName)[0]);
					}
				}
			}
		}
		
		/*
		 * 从request.getAttributeNames()初始化参数
		 */
		Enumeration<String> attributes = req.getAttributeNames();
		Object attribute;
		String attributeName;
		injector = null;
		while(attributes.hasMoreElements()){
			attributeName = attributes.nextElement();
			attribute = req.getAttribute(attributeName);
			
			if(attribute != null){
				injector = actionConfig.injectors.get(attributeName);
				if(injector != null && injector.argumentType.isInstance(attribute)){
					arguments[actionConfig.injectors.get(attributeName).argumentIndex] = attribute;
				}
			}
		}
		
		/* 初始化内置参数 */
		if (actionConfig.builtInArgument != null) {
			for (Class<?> type : actionConfig.builtInArgument.keySet()) {
				if (type.equals(HttpServletRequest.class)) {
					arguments[actionConfig.builtInArgument.get(type)] = req;
				} else if (type.equals(HttpServletResponse.class)) {
					arguments[actionConfig.builtInArgument.get(type)] = resp;
				} else if (type.equals(HttpSession.class)) {
					arguments[actionConfig.builtInArgument.get(type)] = req.getSession();
				} else if (type.equals(Cookie[].class)) {
					arguments[actionConfig.builtInArgument.get(type)] = req.getCookies();
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
