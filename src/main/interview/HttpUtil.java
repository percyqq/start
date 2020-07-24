import com.alibaba.fastjson.JSONObject;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpUtil {


            <dependency>
                <groupId>org.apache.httpcomponents</groupId>
                <artifactId>httpclient</artifactId>
                <version>${httpclient.version}</version>
                <exclusions>
                    <exclusion>
                        <artifactId>commons-logging</artifactId>
                        <groupId>commons-logging</groupId>
                    </exclusion>
                </exclusions>
            </dependency>
            
            
            
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static CloseableHttpClient httpClient;

    // 池化管理
    private static PoolingHttpClientConnectionManager poolConnManager = null;

    static {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
            }};

            SSLContext sl = SSLContext.getInstance("TLSv1.2");
            sl.init(null, trustAllCerts, new java.security.SecureRandom());
            HostnameVerifier f = (String s, SSLSession l) -> {
                return true;
            };
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sl, f);

            // 配置同时支持 HTTP 和 HTPPS
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslsf)
                .build();

            poolConnManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            poolConnManager.setMaxTotal(15);// 默认的是20
            poolConnManager.setDefaultMaxPerRoute(3); // 设置最大路由

            RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(15000) // 设置连接超时时间，单位毫秒。
                .setConnectionRequestTimeout(10000) // 设置从connectManager获取Connection超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
                .setSocketTimeout(15000)
                .build(); // 请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。

            // 初始化httpClient
            httpClient = HttpClients.custom()
                // 设置连接池管理
                .setConnectionManager(poolConnManager)
                // 设置请求配置
                .setDefaultRequestConfig(requestConfig)
                // 设置重试次数
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .setConnectionTimeToLive(30, TimeUnit.SECONDS)
                .build();

            if (poolConnManager != null && poolConnManager.getTotalStats() != null) {
                logger.info("now client pool {}", poolConnManager.getTotalStats().toString());
            }

        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String accessWithIgnoreCert(String url, String method) {
        return accessWithIgnoreCert(url, method, null, null);
    }

    public static String putJson(String url, List<BasicNameValuePair> headers, String jsonParam) {
        HttpPut httpPut = new HttpPut(url);
        logger.info("put method");
        CloseableHttpResponse response = null;
        String result = null;
        try {
            if (null != headers) {
                for (BasicNameValuePair header : headers) {
                    httpPut.setHeader(encodeHeader(header.getName()), encodeHeader(header.getValue()));
                }
            }
            logger.info("put method");
            StringEntity entity = new StringEntity(jsonParam, "utf-8");// 解决中文乱码问题
            logger.info("put method");
            entity.setContentEncoding("UTF-8");
            logger.info("put method");
            entity.setContentType("application/json");
            httpPut.setEntity(entity);
            logger.info("put method");
            response = httpClient.execute(httpPut);
            logger.info("put method");
            if (response != null) {
                logger.info("put method");
                result = EntityUtils.toString(response.getEntity());
            }
        } catch (ClientProtocolException e) {
            logger.info("put method");
            logger.error("HttpClient access get response failed Exceptione", e);
        } catch (IOException e) {
            logger.error("HttpClient access get response failed Exceptione", e);
        } finally {
            logger.info("put method");
            StreamUtil.close(response);
            httpPut.releaseConnection();
        }
        return result;
    }

    public static String postJson(String url, List<BasicNameValuePair> headers, String jsonParam) {
        String result = "";
        if (StringUtils.isEmpty(url)) {
            return result;
        }
        HttpPost httpPut = new HttpPost(url);
        CloseableHttpResponse response = null;
        try {
            if (null != headers) {
                for (BasicNameValuePair header : headers) {
                    httpPut.setHeader(encodeHeader(header.getName()), encodeHeader(header.getValue()));
                }
            }
            StringEntity entity = new StringEntity(jsonParam, "utf-8");// 解决中文乱码问题
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            httpPut.setEntity(entity);

            response = httpClient.execute(httpPut);
            if (response != null) {
                result = EntityUtils.toString(response.getEntity());
            }
        } catch (ClientProtocolException e) {
            logger.error("HttpClient access get response failed Exceptione", e);
        } catch (IOException e) {
            logger.error("HttpClient access get response failed Exceptione", e);
        } finally {
            StreamUtil.close(response);
            httpPut.releaseConnection();
        }
        return result;
    }

    public static String getJson(String url, List<BasicNameValuePair> headers) {
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        String result = null;
        try {
            if (null != headers) {
                for (BasicNameValuePair header : headers) {
                    httpGet.setHeader(encodeHeader(header.getName()), encodeHeader(header.getValue()));
                }
            }

            response = httpClient.execute(httpGet);
            if (response != null) {
                result = EntityUtils.toString(response.getEntity());
            }
        } catch (ClientProtocolException e) {
            logger.error("HttpClient access get response failed Exceptione", e);
        } catch (IOException e) {
            logger.error("HttpClient access get response failed Exceptione", e);
        } finally {
            StreamUtil.close(response);
            httpGet.releaseConnection();
        }
        return result;
    }

    public static String accessWithIgnoreCert(String url, String method, List<BasicNameValuePair> headers,
        List<BasicNameValuePair> parameters) {
        method = method.toUpperCase();
        try {
            String result = execRequest(url, method, headers, parameters, httpClient);
            return result;
        } catch (Exception e) {
            logger.error("HttpClient access get response failed Exceptione", e);
        }
        return null;
    }

    private static String execRequest(String url, String method, List<BasicNameValuePair> headers,
        List<BasicNameValuePair> parameters, CloseableHttpClient httpClient)
        throws ClientProtocolException, IOException {

        HttpRequestBase httpRequest = null;
        CloseableHttpResponse response = null;
        String result = null;
        try {
            // set the method
            httpRequest = getRequest(url, method, headers, parameters);
            if (null == httpRequest) {
                logger.warn("request is null");
                return null;
            }

            response = httpClient.execute(httpRequest);
            if (response != null) {
                result = EntityUtils.toString(response.getEntity());
            }
        } finally {
            StreamUtil.close(response);
            if (httpRequest != null) {
                httpRequest.releaseConnection();
            }

        }
        return result;
    }

    private static HttpRequestBase getRequest(String url, String type, List<BasicNameValuePair> headers,
        List<BasicNameValuePair> parameters) throws UnsupportedEncodingException {
        HttpRequestBase httpRequest;
        switch (type) {
            case "PUT":
                httpRequest = putRequest(url, parameters, headers);
                break;
            case "POST":
                httpRequest = postRequest(url, parameters, headers);
                break;
            case "GET":
                httpRequest = getGetRequest(url, parameters, headers);
                break;
            case "DELETE":
                httpRequest = deleteRequest(url, parameters, headers);
                break;
            default:
                logger.warn("Http method type value error. value: {}", type);
                return null;
        }

        // set headers
        if (null != headers) {
            for (BasicNameValuePair header : headers) {
                httpRequest.setHeader(encodeHeader(header.getName()), encodeHeader(header.getValue()));
            }
        }
        return httpRequest;
    }

    private static HttpRequestBase deleteRequest(String url, List<BasicNameValuePair> parameters,
        List<BasicNameValuePair> headers) throws UnsupportedEncodingException {
        return new HttpDelete(httpGetOrDelParams(url, parameters).toString());
    }

    private static HttpRequestBase getGetRequest(String url, List<BasicNameValuePair> parameters,
        List<BasicNameValuePair> headers) throws UnsupportedEncodingException {
        StringBuffer sb = httpGetOrDelParams(url, parameters);
        return new HttpGet(sb.toString());
    }

    private static StringBuffer httpGetOrDelParams(String url, List<BasicNameValuePair> parameters)
        throws UnsupportedEncodingException {

        StringBuffer sb = new StringBuffer("");
        sb.append(url);
        if (null != parameters) {
            sb.append("?");
            boolean init = false;
            for (BasicNameValuePair e : parameters) {
                if (!init) {
                    sb.append(URLEncoder.encode(e.getName(), "UTF-8"));
                    sb.append("=");
                    sb.append(URLEncoder.encode(e.getValue(), "UTF-8"));
                    init = true;
                } else {
                    sb.append("&");
                    sb.append(URLEncoder.encode(e.getName(), "UTF-8"));
                    sb.append("=");
                    sb.append(URLEncoder.encode(e.getValue(), "UTF-8"));
                }
            }
        }
        return sb;
    }

    private static HttpRequestBase postRequest(String url, List<BasicNameValuePair> parameters,
        List<BasicNameValuePair> headers) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        HttpRequestBase httpRequest = httpPost;
        if (null == parameters) {
            return httpRequest;

        }
        httpPost.setEntity(assemblyParams(parameters, headers));
        return httpRequest;
    }

    private static HttpRequestBase putRequest(String url, List<BasicNameValuePair> parameters,
        List<BasicNameValuePair> headers) throws UnsupportedEncodingException {
        HttpPut httpPut = new HttpPut(url);
        HttpRequestBase httpRequest = httpPut;
        if (null == parameters) {
            return httpRequest;
        }

        httpPut.setEntity(assemblyParams(parameters, headers));
        return httpRequest;
    }

    private static StringEntity assemblyParams(List<BasicNameValuePair> parameters, List<BasicNameValuePair> headers)
        throws UnsupportedEncodingException {
        StringEntity entity;
        int type = 0;
        for (BasicNameValuePair head : headers) {
            String value = head.getValue().trim().toLowerCase();
            if (head.getName().trim().equalsIgnoreCase("Content-Type") && value.equalsIgnoreCase("application/json")) {
                // 是json
                type = 1;
                break;
            }
            if (head.getName().trim().equalsIgnoreCase("Content-Type") && value.contains("multipart/form-data")) {
                // 是表单
                type = 2;
                break;
            }
        }
        return assemblyParamsForType(parameters, type);
    }

    private static StringEntity assemblyParamsForType(List<BasicNameValuePair> parameters, int type)
        throws UnsupportedEncodingException {
        StringEntity entity;
        if (type == 1) {
            JSONObject json = new JSONObject();
            for (BasicNameValuePair param : parameters) {
                json.put(param.getName(), param.getValue());
            }
            entity = new StringEntity(json.toJSONString());
        } else if (type == 2) {
            String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
            StringBuffer sb = new StringBuffer();
            sb.append("--" + boundary + "\r\n");
            for (BasicNameValuePair param : parameters) {
                sb.append("Content-Disposition: form-data; name=\"" + param.getName() + "\"\r\n");
                sb.append("\r\n");
                String value = param.getValue();
                sb.append(value + "\r\n");
                sb.append("--" + boundary + "\r\n");
            }
            entity = new StringEntity(sb.toString());
        } else {
            List<BasicNameValuePair> paramList = encodeList(parameters);
            entity = new UrlEncodedFormEntity(paramList, "UTF-8");
        }
        return entity;
    }

    private static String encodeHeader(Object obj) {
        if (obj == null) {
            return "";
        }
        String msg = obj.toString();
        int length = msg.length();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char ch = msg.charAt(i);
            // 将\r\n替换成'_'
            if (ch == '\r' || ch == '\n') {
                continue;
            }
            sb.append(Character.valueOf(ch));
        }
        return sb.toString();
    }

    private static List<BasicNameValuePair> encodeList(List<BasicNameValuePair> objList) {
        List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
        for (BasicNameValuePair obj : objList) {
            list.add(obj);
        }
        return list;
    }
}
