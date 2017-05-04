package com.zg.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public final class HttpUtils {

    public static final String DEFAULT_CHARSET = "UTF-8";          //设置默认通信报文编码为UTF-8
    private static final int DEFAULT_CONNECTION_TIMEOUT = 1000 * 5; //设置默认连接超时为2s
    private static final int DEFAULT_SO_TIMEOUT = 1000 * 60;        //设置默认读取超时为60s

    private HttpUtils(){}


    /**
     * 发送HTTP_GET请求
     */
    public static String get(String reqURL){
        String respData = "";
        HttpClient httpClient = new DefaultHttpClient();
        //连接超时2s
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
        //读取超时60s
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, DEFAULT_SO_TIMEOUT);
        try{
            httpClient = addTLSSupport(httpClient);
            HttpResponse response = httpClient.execute(new HttpGet(reqURL));
            HttpEntity entity = response.getEntity();
            if(null != entity){
                Charset respCharset = ContentType.getOrDefault(entity).getCharset();
                respData = EntityUtils.toString(entity, respCharset);
                //Consume response content,主要用来关闭输入流的,对于远程返回内容不是流时,不需要执行此方法(这里只是演示)
                EntityUtils.consume(entity);
            }
            return respData;
        }catch(Exception e){
            throw new RuntimeException("请求通信[" + reqURL + "]时遇到异常", e);
        }finally{
            //关闭连接,释放资源
            httpClient.getConnectionManager().shutdown();
        }
    }


    /**
     * 发送HTTP_POST请求
     */
    public static String post(String reqURL, String reqData, String contentType){
        String respData = "";
        HttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, DEFAULT_SO_TIMEOUT);
        HttpPost httpPost = new HttpPost(reqURL);
        //由于下面使用的是new StringEntity(....),所以默认发出去的请求报文头中CONTENT_TYPE值为text/plain; charset=ISO-8859-1
        //这就有可能会导致服务端接收不到POST过去的参数,比如运行在Tomcat6.0.36中的Servlet,所以我们手工指定CONTENT_TYPE头消息
        if(StringUtils.isBlank(contentType)){
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=" + DEFAULT_CHARSET);
        }else{
            httpPost.setHeader(HTTP.CONTENT_TYPE, contentType);
        }
        httpPost.setEntity(new StringEntity(null==reqData?"":reqData, DEFAULT_CHARSET));
        try{
            httpClient = addTLSSupport(httpClient);
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if(null != entity){
                respData = EntityUtils.toString(entity, ContentType.getOrDefault(entity).getCharset());
            }
            return respData;
        }catch(Exception e){
            throw new RuntimeException("请求通信[" + reqURL + "]时遇到异常", e);
        }finally{
            httpClient.getConnectionManager().shutdown();
        }
    }


    /**
     * 发送HTTP_POST请求
     */
    public static String post(String reqURL, Map<String, String> params){
        String respData = "";
        HttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, DEFAULT_SO_TIMEOUT);
        try {
            HttpPost httpPost = new HttpPost(reqURL);
            //由于下面使用的是new UrlEncodedFormEntity(....),所以这里不需要手工指定CONTENT_TYPE为application/x-www-form-urlencoded
            //因为在查看了HttpClient的源码后发现,UrlEncodedFormEntity所采用的默认CONTENT_TYPE就是application/x-www-form-urlencoded
            //httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=" + encodeCharset);
            if(null != params){
                List<NameValuePair> formParams = new ArrayList<>();
                for(Map.Entry<String,String> entry : params.entrySet()){
                    formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(formParams, DEFAULT_CHARSET));
            }
            httpClient = addTLSSupport(httpClient);
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if(null != entity){
                respData = EntityUtils.toString(entity, ContentType.getOrDefault(entity).getCharset());
            }
            return respData;
        }catch(Exception e){
            throw new RuntimeException("请求通信[" + reqURL + "]时遇到异常", e);
        }finally{
            httpClient.getConnectionManager().shutdown();
        }
    }

    private static HttpClient addTLSSupport(HttpClient httpClient) throws NoSuchAlgorithmException, KeyManagementException {
        //创建TrustManager(),用于解决javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated
        X509TrustManager trustManager = new X509TrustManager(){
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            @Override
            public X509Certificate[] getAcceptedIssuers() {return null;}
        };
        //创建HostnameVerifier,用于解决javax.net.ssl.SSLException: hostname in certificate didn't match: <123.125.97.66> != <123.125.97.241>
        X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier(){
            @Override
            public void verify(String host, SSLSocket ssl) throws IOException {}
            @Override
            public void verify(String host, X509Certificate cert) throws SSLException {}
            @Override
            public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {}
            @Override
            public boolean verify(String arg0, SSLSession arg1) {return true;}
        };
        //TLS1.0是SSL3.0的升级版(网上已有人发现SSL3.0的致命BUG了),它们使用的是相同的SSLContext
        SSLContext sslContext = SSLContext.getInstance(SSLSocketFactory.TLS);
        //使用TrustManager来初始化该上下文,TrustManager只是被SSL的Socket所使用
        sslContext.init(null, new TrustManager[]{trustManager}, null);
        //创建SSLSocketFactory
        SSLSocketFactory socketFactory = new SSLSocketFactory(sslContext, hostnameVerifier);
        //通过SchemeRegistry将SSLSocketFactory注册到HttpClient上
        httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory));
        return httpClient;
    }
}