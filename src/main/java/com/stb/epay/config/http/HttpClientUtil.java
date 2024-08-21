package com.stb.epay.config.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.Timeout;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class HttpClientUtil {

    private final HttpClient httpClient; // default
    private final Integer cfMaxTotal;
    private final Integer cfMaxPerRout;
    private final int TIMEOUT_ZERO = 0;
    private final String POST = "POST";
    private final String GET = "GET";
    private Map<String, HttpClient> httpClientsMap = new HashMap<>();

    public String sendPost(String url, String body) {
        return sendPost(url, body, null, null, null);
    }

    public String sendPost(String url, String body, int timeout) {
        return getHttpData(url, null, null, null, POST, body, timeout);
    }

    public String sendPost(String url, String body, String contentType, String accept, Map<String, String> headers) {
        return getHttpData(url, contentType, accept, headers, POST, body, TIMEOUT_ZERO);
    }

    public String sendPost(String url, String body, String contentType, String accept, Map<String, String> headers, int timeout) {
        return getHttpData(url, contentType, accept, headers, POST, body, timeout);
    }

    public Map<String, Map<String, String>> sendPostClassic(String url, String body, String contentType, String accept, Map<String, String> headers, int timeout) {
        return getClassicHttpResponse(url, contentType, accept, headers, POST, body, timeout);
    }

    public String sendGet(String url) {
        return sendGet(url, null, null, null);
    }

    public String sendGet(String url, String contentType, String accept, Map<String, String> headers) {
        return getHttpData(url, contentType, accept, headers, GET, null, TIMEOUT_ZERO);
    }

    private String getHttpData(String url, String contentType, String accept, Map<String, String> headers, String method, String body, int timeout) {
        HttpUriRequestBase httpUri;
        if (POST.equals(method)) {
            httpUri = new HttpPost(url);
            httpUri.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        } else {
            httpUri = new HttpGet(url);
        }

        httpUri.addHeader("Content-Type", StringUtils.hasText(contentType) ? contentType : MimeTypeUtils.APPLICATION_JSON_VALUE);
        httpUri.addHeader("Accept", StringUtils.hasText(accept) ? accept : MimeTypeUtils.APPLICATION_JSON_VALUE);
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach(httpUri::addHeader);
        }
        try {
            HttpClient client = getHttpClient(url, timeout);
            return client == null ? null :
                    client.execute(httpUri, response -> {
                        final HttpEntity responseEntity = response.getEntity();
                        String data = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                        EntityUtils.consume(responseEntity);
                        return data;
                    });
        } catch (IOException e) {
            log.warn(e.toString());
            return "Timeout";
        }
    }

    private Map<String, Map<String, String>> getClassicHttpResponse(String url, String contentType, String accept, Map<String, String> headers, String method, String body, int timeout) {
        HttpUriRequestBase httpUri;
        if (POST.equals(method)) {
            httpUri = new HttpPost(url);
            httpUri.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        } else {
            httpUri = new HttpGet(url);
        }

        httpUri.addHeader("Content-Type", StringUtils.hasText(contentType) ? contentType : MimeTypeUtils.APPLICATION_JSON_VALUE);
        httpUri.addHeader("Accept", StringUtils.hasText(accept) ? accept : MimeTypeUtils.APPLICATION_JSON_VALUE);
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach(httpUri::addHeader);
        }
        try {
            HttpClient client = getHttpClient(url, timeout);
            Map<String, Map<String, String>> httpResponse = new HashMap<>();
            return client == null ? null :
                    client.execute(httpUri, response -> {
                        final HttpEntity responseEntity = response.getEntity();
                        Map<String, String> httpHeader = new HashMap<>();
                        Map<String, String> httpBody = new HashMap<>();
                        for (Header header : response.getHeaders()) {
                            httpHeader.put(header.getName(), header.getValue());
                        }
                        httpResponse.put("Headers", httpHeader);
                        String data = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                        httpBody.put("Data", data);
                        httpResponse.put("Body", httpBody);
                        return httpResponse;
                    });
        } catch (IOException e) {
            log.warn(e.toString());
            return null;
        }
    }

    private HttpClient getHttpClient(String url, int timeout) {
        if (timeout == TIMEOUT_ZERO) {
            return httpClient;
        }
        String key = url + timeout;
        if (httpClientsMap.containsKey(key)) {
            return httpClientsMap.get(key);
        } else {
            HttpClient httpClientTimeout = createClient(cfMaxTotal, cfMaxPerRout, timeout);
            if (httpClientTimeout != null) {
                httpClientsMap.put(key, httpClientTimeout);
            }
            return httpClientTimeout;
        }
    }

    public static HttpClient createClient(Integer cfMaxTotal, Integer cfMaxPerRout, Integer timeout) {
        try {
            TrustStrategy ts = (arg0, arg1) -> true;
            final SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, ts).build();
            timeout = Optional.ofNullable(timeout).orElse(2 * 60 * 1000);

            RequestConfig config = RequestConfig.custom()
                    .setConnectionRequestTimeout(Timeout.of(timeout, TimeUnit.MILLISECONDS))
                    .build();

            PoolingHttpClientConnectionManager cnnMgr = PoolingHttpClientConnectionManagerBuilder.create()
                    .setMaxConnTotal(Optional.ofNullable(cfMaxTotal).orElse(10000))
                    .setMaxConnPerRoute(Optional.ofNullable(cfMaxPerRout).orElse(5000))
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, new DefaultHostnameVerifier()))
                    .setDefaultConnectionConfig(ConnectionConfig.custom()
                            .setConnectTimeout(Timeout.of(timeout, TimeUnit.MILLISECONDS))
                            .setSocketTimeout(Timeout.of(timeout, TimeUnit.MILLISECONDS))
                            .build())
                    .build();
            return HttpClients.custom()
                    .setDefaultRequestConfig(config)
                    .setConnectionManager(cnnMgr)
                    .build();
        } catch (Exception e) {
            log.warn("Create http client fail. Error: {}", e.toString());
        }
        return null;
    }
}
