package uri;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.junit.Test;

public class TestRegex {
	@Test
	public void testReg() throws UnsupportedEncodingException{
		String str = "bp=1&tn=baidu&wd=http%20get%20编码&rsv_pq=85de1a"
				+ "4800014f3b&rsv_t=c7b04YhMRIPJBTu%2FnQh8notZHbuUTUPPa"
				+ "tpnt1p7JyAYpdKtOY64vBf1YMM&rsv_enter=1&rsv_sug3=8&rsv_"
				+ "sug1=7&rsv_sug2=0&inputT=10110&rsv_sug4=10110";
		
		int a = 100000;
		for(int i=0; i<a; i++){
			//str.contains("%");
				URLDecoder.decode(str,  "utf8");
				// TODO Auto-generated catch block
		}
	}
}
