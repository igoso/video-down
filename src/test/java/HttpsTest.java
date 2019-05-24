import com.igoso.me.xdf.util.HttpClientManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import java.io.IOException;

/**
 * created by igoso at 2018/11/1
 **/
public class HttpsTest {

    private static final String MY_URL = "https://www.igosh.com/oss";
    private static final String BAIDU_URL = "https://www.baidu.com";
    private static final String BAIDU_HTTP_URL = "http://www.baidu.com";


    @Test
    public void httpsTest() throws IOException {
        HttpResponse response;
        CloseableHttpClient client = HttpClientManager.getClient();
        HttpGet get1 = new HttpGet(MY_URL);
        response = client.execute(get1);
        System.out.println(response.getEntity().getContent());

        HttpGet get2 = new HttpGet(BAIDU_URL);
        response = client.execute(get2);
        System.out.println(response.getEntity().getContent());

        HttpGet get3 = new HttpGet(BAIDU_HTTP_URL);
        response = client.execute(get3);
        System.out.println(response.getEntity().getContent());

    }
}
