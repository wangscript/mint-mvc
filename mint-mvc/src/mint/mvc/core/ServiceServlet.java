package mint.mvc.core;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.istack.internal.logging.Logger;

/**
 * 
 * 
 * 
 */

/**
 * 
 * @Description: DispatcherServlet must be mapped to root URL "/". It handles ALL requests 
 * from clients, and dispatches to appropriate handler to handle each request.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:10:43 
 *
 */
@WebServlet(asyncSupported = true) 
public class ServiceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final Logger logger = Logger.getLogger(this.getClass());

    private Dispatcher dispatcher;
    private StaticFileHandler staticFileHandler;
    private ActionExecutor actionExecutor; 
    
    @Override
    public void init(final ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        logger.info("Init ServiceServlet...");
        this.dispatcher = new Dispatcher();
        this.actionExecutor = new ActionExecutor();
        
        Config config = new Config() {
            public String getInitParameter(String name) {
                return servletConfig.getInitParameter(name);
            }

            public ServletContext getServletContext() {
                return servletConfig.getServletContext();
            }
        };
        
        this.dispatcher.init(config);
        this.actionExecutor.init(config);
        
        this.staticFileHandler = new StaticFileHandler(servletConfig);
    }

    @Override
    public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
        HttpServletRequest 	httpReq 	= (HttpServletRequest) req;
        HttpServletResponse httpResp 	= (HttpServletResponse) resp;
        String 				method 		= httpReq.getMethod();
        
        if ("GET".equals(method) || "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
        	/*进入处理请求*/
        	Action action = dispatcher.dispatch(httpReq);
            if (action != null) {
            	actionExecutor.executeAction(httpReq, httpResp, action);
            } else {
            	staticFileHandler.handle(httpReq, httpResp);
            }
            return;
        }
        
        httpResp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    public void destroy() {
        logger.info("Destroy DispatcherServlet...");
        this.dispatcher.destroy();
    }
}