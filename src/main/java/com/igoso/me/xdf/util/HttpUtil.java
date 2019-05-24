package com.igoso.me.xdf.util;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

/**
 * created by igoso at 2018/10/17
 **/
public class HttpUtil {
    private static final String UA_IPAD = "Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1";
    private static final String REFERER = "http://www.koolearn.com/ke/ielts/";
    private static final String ORIGIN = "https://login.koolearn.com";
    private static final String UA_MAC_CHROME = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36";
    public static final String UA_ANDROID = "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.67 Mobile Safari/537.36";

    public static volatile String COOKIE_HEADER = "";
    /**
     * http normal get
     *
     * @param url
     * @param client
     * @return
     */
    public static String httpGet(String url, HttpClient client) throws Exception {
        HttpGet get = new HttpGet(url);
        HttpResponse httpResponse;
        HttpEntity httpEntity = null;
        try {
            get.addHeader("User-Agent", UA_ANDROID);
            get.addHeader("Cookie", COOKIE_HEADER);
//            get.addHeader("Referer",REFERER);
            get.addHeader("Origin", "https://login.koolearn.com");
            httpResponse = client.execute(get);
            httpEntity = httpResponse.getEntity();
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(httpEntity, CharEncoding.UTF_8);
            }
        } catch (Exception e) {
            throw e;
        }

        return null;
    }

    public static void setCookieHeader(String cookieHeader) {
            COOKIE_HEADER = cookieHeader;
    }

    /**
     * http do base url encode post
     *
     * @param url
     * @param client
     * @param valuePairs
     * @param origin
     * @return
     */
    public static String httpPost(String url, HttpClient client, List<NameValuePair> valuePairs, String origin) throws Exception {
        HttpPost post = new HttpPost(url);
        HttpResponse httpResponse;
        HttpEntity httpEntity = null;
        try {
            post.addHeader("User-Agent", UA_MAC_CHROME);
//            get.addHeader("Cookie",COOKIE);
            post.addHeader("Referer", REFERER);
            if (origin != null) {
                post.addHeader("Origin", "https://login.koolearn.com");
            }
            post.setEntity(new UrlEncodedFormEntity(valuePairs, CharEncoding.UTF_8));
            httpResponse = client.execute(post);
            httpEntity = httpResponse.getEntity();
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(httpEntity);
            }
        } catch (Exception e) {
            throw e;
        }

        return null;
    }

    /**
     * get byte from remote url
     *
     * @param url
     * @param client
     * @return
     * @throws IOException
     */
    public static byte[] httpGetByte(String url, HttpClient client) throws Exception {

        HttpGet get = new HttpGet(url);
        HttpResponse httpResponse;
        HttpEntity httpEntity;
        try {
            get.addHeader("User-Agent", UA_IPAD);
            get.addHeader("Cookie", COOKIE_HEADER);
            get.addHeader("Referer", REFERER);
            get.addHeader("Origin", "https://login.koolearn.com");
            httpResponse = client.execute(get);
            httpEntity = httpResponse.getEntity();
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toByteArray(httpEntity);
            } else {
                EntityUtils.toByteArray(httpEntity);
            }
        } catch (Exception e) {
            throw e;
        }
        return null;
    }
}
