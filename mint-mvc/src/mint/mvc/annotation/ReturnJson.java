package mint.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 配置了该注解的action方法回将返回值自动序列化成json返回
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:25:49 
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReturnJson {
}