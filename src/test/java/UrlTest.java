import com.igoso.me.xdf.executor.ExecutorUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

/**
 * created by igoso at 2018/10/24
 **/
public class UrlTest {
    public static void main(String[] args) throws UnsupportedEncodingException {
        String url = "http://www.baidu.com/?dfsdfds%2Bsdfdsdsgdsg%20sdfdsgnd fnbviodn";
        System.out.println("1 org--" + url);
        url = URLDecoder.decode(url,"UTF-8");
        System.out.println("2 decode---" + url);

        url = URLDecoder.decode(url,"UTF-8");
        System.out.println("2 decode---" + url);

        url = URLEncoder.encode(url, "UTF-8");
        System.out.println("3 encode---" + url);

        url = URLDecoder.decode(url, "UTF-8");
        System.out.println("4 decode---" +url);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("MY_LOGGER");

    @Test
    public void logTest() {
        for (int i = 0; i < 10; i++) {
            LOGGER.info("get log id :{}",i);
        }
    }

    @Test
    public void scheduledTest() throws InterruptedException {
        ExecutorUtil.monitorProcess();
        TimeUnit.HOURS.sleep(1);
    }
}
