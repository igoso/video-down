package com.igoso.me.xdf.util;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;

/**
 * created by igoso at 2018/11/1
 **/
public class HttpClientManager {

    private static final HttpClientManager INSTANCE = new HttpClientManager();
    private static HttpClientBuilder builder;
    private static PoolingHttpClientConnectionManager manager;

    private HttpClientManager(){
        manager = new PoolingHttpClientConnectionManager(getSocketFactoryRegistry());
        manager.setMaxTotal(100);
        manager.setDefaultMaxPerRoute(10);
        builder = HttpClients.custom().setConnectionManager(manager);
    }


    public static CloseableHttpClient getClient() {
        return builder.build();
    }


    private Registry<ConnectionSocketFactory> getSocketFactoryRegistry() {
        SSLConnectionSocketFactory socketFactory = sslConnectionSocketFactory();
        if (socketFactory == null) {
            socketFactory = SSLConnectionSocketFactory.getSocketFactory();
        }
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", socketFactory).build();
    }

    private SSLConnectionSocketFactory sslConnectionSocketFactory() {
        try {
            SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(null,null,null);
            return new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            return null;
        }
    }

}
