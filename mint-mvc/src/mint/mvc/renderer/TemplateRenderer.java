package mint.mvc.renderer;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mint.mvc.template.TemplateFactory;

/**
 * Render output using template engine.
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:15:02 
 *
 */
public class TemplateRenderer extends Renderer {

    private String path;
    private Map<String, Object> model;

    /**
     * @param path
     */
    public TemplateRenderer(String path) {
        this.path = path;
        this.model = new HashMap<String, Object>();
    }

    /**
     * @param path
     * @param model
     */
    public TemplateRenderer(String path, Map<String, Object> model) {
        this.path = path;
        this.model = model;
    }

    /**
     * @param path 模板路径
     * @param modelKey
     * @param modelValue
     */
    public TemplateRenderer(String path, String modelKey, Object modelValue) {
        this.path = path;
        this.model = new HashMap<String, Object>();
        this.model.put(modelKey, modelValue);
    }

    @Override
    public void render(ServletContext context, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	if(model == null){
    		model = new HashMap<String, Object>();
    	}
    	
    	/*内置baseUrl*/
    	StringBuilder baseUrl = new StringBuilder(255);
    	baseUrl.append(request.getScheme()).append("://").append(request.getServerName()).append(":").append(request.getServerPort());
    	String contextPath = request.getContextPath();
    	
    	if(contextPath != null){
    		baseUrl.append(contextPath).append("/");
    	}
    	
    	model.put("baseUrl", baseUrl.toString());
        TemplateFactory.getTemplateFactory()
                .loadTemplate(path)
                .render(request, response, model);
    }

}
