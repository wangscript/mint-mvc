package mint.mvc.core;

import mint.mvc.template.TemplateFactory;

import com.sun.istack.internal.logging.Logger;

/**
 * Utils for create ContainerFactory, TemplateFactory, etc.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
class Utils {
    TemplateFactory createTemplateFactory(String name) {
        TemplateFactory tf = tryInitTemplateFactory(name);
        if (tf==null)
            tf = tryInitTemplateFactory(TemplateFactory.class.getPackage().getName() + "." + name + TemplateFactory.class.getSimpleName());
        if (tf==null) {
        	Logger.getLogger(Utils.class).warning("Cannot init template factory '" + name + "'.");
            throw new ConfigException("Cannot init template factory '" + name + "'.");
        }
        return tf;
    }

    TemplateFactory tryInitTemplateFactory(String clazz) {
        try {
            Object obj = Class.forName(clazz).newInstance();
            if (obj instanceof TemplateFactory)
                return (TemplateFactory) obj;
        }
        catch(Exception e) { }
        return null;
    }
}
