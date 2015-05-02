package demo.action;

import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mint.mvc.renderer.Renderer;

import com.alibaba.fastjson.JSON;

/** 
 * 另一种返回json的方式
 * 
 * @author LiangWei(895925636@qq.com)
 * @date 2015年5月1日 下午11:39:46 
 *  
 */
public class JSONRenderer extends Renderer{
	private Object obj;
	
	JSONRenderer (Object obj){
		this.obj = obj;
	}
	
	@Override
	public void render(ServletContext ctx, HttpServletRequest req,
			HttpServletResponse resp) throws Exception {
		
		resp.setContentType("application/json;charset=UTF-8");
		PrintWriter pw = resp.getWriter();
		pw.write(JSON.toJSONString(obj));
		pw.flush();
	}
}