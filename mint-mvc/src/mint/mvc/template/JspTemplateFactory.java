package mint.mvc.template;

import mint.mvc.core.Config;

import com.sun.istack.internal.logging.Logger;

/**
 * TemplateFactory which uses JSP.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class JspTemplateFactory extends TemplateFactory {
	private final Logger logger = Logger.getLogger(this.getClass());

    public Template loadTemplate(String path) throws Exception {
        return new JspTemplate(path);
    }

    public void init(Config config) {
        logger.info("JspTemplateFactory init ok.");
    }

}
