package com.igoso.me.xdf;

import com.igoso.me.xdf.executor.ExecutorUtil;
import com.igoso.me.xdf.executor.MediaLoader;
import com.igoso.me.xdf.util.ClientUtil;
import com.igoso.me.xdf.util.HttpUtil;
import com.igoso.me.xdf.util.TimeUtil;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.proxy.CaptureType;
import org.apache.commons.lang3.StringUtils;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Main Application Portal
 * created by igoso at 2018/10/26
 **/
public class Application {
    private static final Logger MEDIA_LOGGER = LoggerFactory.getLogger("MEDIA");
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        LOGGER.info("application start");
        System.setProperty("webdriver.chrome.driver", "runtime"+ File.separator +"chromedriver");
        BrowserMobProxy proxy = new BrowserMobProxyServer();
        proxy.start(0);
        Proxy seleniumProxy = net.lightbody.bmp.client.ClientUtil.createSeleniumProxy(proxy);
        // configure it as a desired capability
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);

        // start the browser up
        ChromeDriver driver = new ChromeDriver(capabilities);
        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);

        LOGGER.info("init 100 http client for readying");
        ClientUtil.initPool(100);

//        proxy.removeHeader("User-Agent");
//        proxy.addHeader("User-Agent", HttpUtil.UA_ANDROID);


        String videoUrl = "http://www.koolearn.com/ke/kaoyan2/";
        // create a new HAR with the label "yahoo.com"
        proxy.newHar("xdf");
        // open yahoo.com
        driver.get(videoUrl);
        LOGGER.info("get default video web page:{}",videoUrl);


        //monitor thread
        LOGGER.info("start monitor thread pool process");
        ExecutorUtil.monitorProcess();

        LOGGER.info("start filter web response");
        proxy.addResponseFilter((response, contents, messageInfo) -> {
            if (contents == null) {
                return;
            }
            String content = contents.getTextContents();
            if (content == null) {
                return;
            }
            if (content.contains("m3u8")) {
                String title = "视频" + TimeUtil.currentDate();
                try {
//                    driver.get(messageInfo.getOriginalUrl());
                    title = driver.getTitle();
                } catch (Exception e) {
                    LOGGER.error("get title from driver error !",e);
                }

                if (StringUtils.isEmpty(title)) {
                    title = TimeUtil.currentDate();
                }
                MEDIA_LOGGER.info("get m3u8,title:{},m3u8:{}",title,content);
                String result = MediaLoader.createLoadTask(title,content);
                MEDIA_LOGGER.info("add media task result:{}",result);
            }
        });

        //remove default proxy headers
        proxy.addLastHttpFilterFactory(new HttpFiltersSourceAdapter() {
            public HttpFilters filterRequest(HttpRequest originalRequest) {
                return new HttpFiltersAdapter(originalRequest) {
                    public HttpResponse proxyToServerRequest(HttpObject httpObject) {
                        if (httpObject instanceof HttpRequest) {
                            ((HttpRequest) httpObject).headers().remove(HttpHeaders.Names.VIA);
                            if(((HttpRequest) httpObject).getUri().startsWith("/common/learning/getNewVideoInfo")){
                                String cookie =((HttpRequest)httpObject).headers().get(HttpHeaders.Names.COOKIE);
                                HttpUtil.setCookieHeader(cookie);
                            }
                        }
                        return null;
                    }
                };
            }
        });


        while (!MediaLoader.isRunning()) {
            try {
                TimeUnit.SECONDS.sleep(15);
                LOGGER.info("running ");
            } catch (Exception e) {
                LOGGER.error("inter exception !",e);
            }
        }
    }
}
