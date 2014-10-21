package mint.mvc.template;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Template using JSP which forward to specific JSP page.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class JspTemplate implements Template {

    private String path;

    public JspTemplate(String path) {
        this.path = path;
    }

    /**
     * Execute the JSP with given model.
     */
    public void render(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) throws Exception {
        for (String key : model.keySet()) {
            request.setAttribute(key, model.get(key));
        }
        request.getRequestDispatcher(path).forward(request, response);
    }

}
