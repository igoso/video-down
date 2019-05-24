package com.igoso.me.xdf.util;

/**
 * created by igoso at 2018/10/17
 **/

import org.apache.http.client.CookieStore;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Random;


public class ClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(ClientUtil.class);

    private static final ArrayList<CloseableHttpClient> simpleClientPool = new ArrayList<>();

//    public static HttpClient getHttpClient(HttpClient base) {
//        try {
//            SSLContext ctx = SSLContext.getInstance("SSL");
//            X509TrustManager tm = new X509TrustManager() {
//                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                    return null;
//                }
//
//                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                }
//
//                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                }
//            };
//
//            ctx.init(null, new TrustManager[]{tm}, null);
//            SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//            ClientConnectionManager mgr = base.getConnectionManager();
//            SchemeRegistry registry = mgr.getSchemeRegistry();
//            registry.register(new Scheme("https", 443, ssf));
//            return new DefaultHttpClient(mgr, base.getParams());
//        } catch (Exception e) {
//            logger.warn("{}", e);
//            return null;
//        }
//    }

    //recommend 100
    public static void initPool(int size) {
        for (int i = 0; i < size; i++) {
            simpleClientPool.add(getHttpClient(null));
        }
    }

    //简单的一个随机选择池
    public static CloseableHttpClient getPooledHttpClient() {
        if (simpleClientPool.size() == 0) {
            return getHttpClient(null);
        }
        int id = new Random().nextInt(simpleClientPool.size());
        return simpleClientPool.get(id);
    }


    public static CloseableHttpClient getHttpClient(CookieStore cookieStore) {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            if (cookieStore == null) {
                cookieStore = new BasicCookieStore();
            }
            return HttpClientBuilder.create().setSSLSocketFactory(socketFactory).setDefaultCookieStore(cookieStore).build();
        } catch (Exception e) {
            logger.warn("create closeable http client failed!");
            return HttpClientBuilder.create().build();
        }
    }


}

