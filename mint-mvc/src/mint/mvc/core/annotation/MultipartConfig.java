package mint.mvc.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MultipartConfig {
	/**
	 * 保存文件的临时文件
	 * @return
	 */
	String tempFilePath();
	
	/**
	 * 表单上传完毕后，保存在request attribute中的名字
	 * @return
	 */
	String attributeName();
	
	/**
	 * 针对该 multipart/form-data 请求的最大数量，默认值为 -1，表示没有限制，小于等于零都表示没限制。
	 * @return
	 */
	int maxRequestSize() default -1;
	
	/**
	 * 请求文件和普通参数长度
	 * @return
	 */
	long limitSize();
}
