package mint.mvc.template;

import mint.mvc.core.Config;

import com.sun.istack.internal.logging.Logger;

/**
 * @Description: TemplateFactory which uses JSP.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:17:18 
 *
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
