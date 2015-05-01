package mint.mvc.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Central handler for handling all exceptions.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public interface ExceptionListener {

    /**
     * Handle exception when exception occurs in actions or interceptor.
     * 
     * @param request The HttpServletRequest object.
     * @param response The HttpServletResponse object.
     * @param e The target exception.
     * @throws Exception If exception occurs or re-throw when handling.
     */
    void handle(HttpServletRequest request, HttpServletResponse response, Exception e) throws Exception;

}