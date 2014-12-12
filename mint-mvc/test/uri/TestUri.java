package uri;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class TestUri {
	
	@Test
	public void test(){
		URI uri;
		try {
			uri = new URI("http://example.com/a%2Fb%3Fc");
			for(String pathSegment : uri.getRawPath().split("/"))
				System.err.println(pathSegment);
			
			System.out.println(uri.getRawPath());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
