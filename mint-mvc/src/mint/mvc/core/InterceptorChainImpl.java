package mint.mvc.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for holds an interceptor chain.
 * @author Michael Liao (askxuefeng@gmail.com)
 */
class InterceptorChainImpl implements InterceptorChain {
    private final 	List<Interceptor> interceptors = new ArrayList<Interceptor>();
    private int 	index 	= 0;
    private boolean isPass 	= false;
    
    boolean isPass() {
        return isPass;
    }
    
    InterceptorChainImpl(Interceptor[] interceptors, String url) {
    	for(Interceptor itcp : interceptors){
    		if(itcp.matchers(url)){
    			this.interceptors.add(itcp);
    		}
    	}
    }

    public void doInterceptor(ActionContext ctx) throws Exception {
        if(index == interceptors.size()){
        	this.isPass = true;
        } else {
            //must update index first, otherwise will cause stack overflow:
            index++;
            interceptors.get(index-1).intercept(ctx, this);
        }
    }
}
