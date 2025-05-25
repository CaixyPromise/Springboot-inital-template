package com.caixy.adminSystem.common.base.utils.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Http请求工具类
 */
public class HttpUtils
{
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // region GET方法
    /**
     * 发送 GET 请求
     */
    public static HttpResponse doGet(String url) throws IOException
    {
        return doGet(url, null, null);
    }

    /**
     * 发送 GET 请求（可带查询参数，但无请求头）
     */
    public static HttpResponse doGet(String url, Map<String, String> queryParams) throws IOException
    {
        return doGet(url, queryParams, null);
    }

    /**
     * 发送 GET 请求（可带查询参数、请求头）
     */
    public static HttpResponse doGet(String url,
                                     Map<String, String> queryParams,
                                     Map<String, String> headers)
            throws IOException {
        String realUrl = buildUrl(url, queryParams);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(realUrl);

        setHeaders(request, headers);
        return client.execute(request);
    }
    // endregion

    // region POST方法

    /**
     * 发送 POST 请求（表单：application/x-www-form-urlencoded）
     */
    public static HttpResponse doPost(String url,
                                      Map<String, String> queryParams,
                                      Map<String, String> headers,
                                      Map<String, String> formData)
            throws IOException
    {
        String realUrl = buildUrl(url, queryParams);
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(realUrl);

        if (headers != null)
        {
            for (Map.Entry<String, String> e : headers.entrySet())
            {
                request.setHeader(e.getKey(), e.getValue());
            }
        }

        if (formData != null && !formData.isEmpty())
        {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            for (Map.Entry<String, String> entry : formData.entrySet())
            {
                nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
            request.setEntity(formEntity);
        }

        return client.execute(request);
    }

    /**
     * 发送 POST 请求（纯文本字符串，如 JSON、XML 等）
     */
    public static HttpResponse doPost(String url,
                                      Map<String, String> queryParams,
                                      Map<String, String> headers,
                                      String body)
            throws IOException
    {
        String realUrl = buildUrl(url, queryParams);
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(realUrl);

        if (headers != null)
        {
            for (Map.Entry<String, String> e : headers.entrySet())
            {
                request.setHeader(e.getKey(), e.getValue());
            }
        }

        if (StringUtils.isNotBlank(body))
        {
            StringEntity entity = new StringEntity(body, "UTF-8");
            // 若是json字符串，可显式设置
            // request.setHeader("Content-Type", "application/json");
            request.setEntity(entity);
        }

        return client.execute(request);
    }

    /**
     * 发送 POST 请求（字节流，如文件上传、二进制数据）
     */
    public static HttpResponse doPost(String url,
                                      Map<String, String> queryParams,
                                      Map<String, String> headers,
                                      byte[] data)
            throws IOException
    {
        String realUrl = buildUrl(url, queryParams);
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(realUrl);

        if (headers != null)
        {
            for (Map.Entry<String, String> e : headers.entrySet())
            {
                request.setHeader(e.getKey(), e.getValue());
            }
        }

        if (data != null && data.length > 0)
        {
            ByteArrayEntity entity = new ByteArrayEntity(data);
            request.setEntity(entity);
        }

        return client.execute(request);
    }

    /**
     * 发送 POST 请求（对象序列化为 JSON 作为请求体）
     */
    public static HttpResponse doPost(String url,
                                      Map<String, String> queryParams,
                                      Map<String, String> headers,
                                      Object bodyObject)
            throws IOException
    {
        String jsonBody = objectMapper.writeValueAsString(bodyObject);

        if (headers == null)
        {
            headers = new HashMap<>();
        }
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE))
        {
            headers.put(HttpHeaders.CONTENT_TYPE, "application/json");
        }

        return doPost(url, queryParams, headers, jsonBody);
    }

    /**
     * POST 表单请求，无 queryParams
     */
    public static HttpResponse doPost(String url,
                                      Map<String, String> headers,
                                      Map<String, String> formData) throws IOException
    {
        return doPost(url, null, headers, formData);
    }

    /**
     * POST 字符串请求体，无 queryParams
     */
    public static HttpResponse doPost(String url,
                                      Map<String, String> headers,
                                      String body) throws IOException
    {
        return doPost(url, null, headers, body);
    }

    /**
     * POST 对象 JSON 请求体，无 queryParams
     */
    public static HttpResponse doPost(String url,
                                      Map<String, String> headers,
                                      Object bodyObject) throws IOException
    {
        return doPost(url, null, headers, bodyObject);
    }

    /**
     * POST 字节流体，无 queryParams
     */
    public static HttpResponse doPost(String url,
                                      Map<String, String> headers,
                                      byte[] data) throws IOException
    {
        return doPost(url, null, headers, data);
    }
    // endregion

    // region PUT 请求
    /**
     * 发送 PUT 请求（纯文本字符串）
     */
    public static HttpResponse doPut(String url,
                                     Map<String, String> queryParams,
                                     Map<String, String> headers,
                                     String body)
            throws IOException {
        String realUrl = buildUrl(url, queryParams);
        HttpClient client = HttpClientBuilder.create().build();
        HttpPut request = new HttpPut(realUrl);

        setHeaders(request, headers);

        if (StringUtils.isNotBlank(body)) {
            StringEntity entity = new StringEntity(body, "UTF-8");
            request.setEntity(entity);
        }

        return client.execute(request);
    }

    /**
     * 发送 PUT 请求（对象序列化为 JSON）
     */
    public static HttpResponse doPut(String url,
                                     Map<String, String> queryParams,
                                     Map<String, String> headers,
                                     Object bodyObject) throws IOException
    {
        String realUrl = buildUrl(url, queryParams);
        HttpClient client = HttpClientBuilder.create().build();
        HttpPut request = new HttpPut(realUrl);

        if (headers == null) {
            headers = new HashMap<>();
        }
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.put(HttpHeaders.CONTENT_TYPE, "application/json");
        }

        setHeaders(request, headers);

        if (bodyObject != null) {
            String jsonBody = objectMapper.writeValueAsString(bodyObject);
            StringEntity entity = new StringEntity(jsonBody, "UTF-8");
            request.setEntity(entity);
        }

        return client.execute(request);
    }

    /**
     * PUT 字符串体，无 queryParams
     */
    public static HttpResponse doPut(String url,
                                     Map<String, String> headers,
                                     String body) throws IOException
    {
        return doPut(url, null, headers, body);
    }

    /**
     * PUT 对象体（序列化为 JSON），无 queryParams
     */
    public static HttpResponse doPut(String url,
                                     Map<String, String> headers,
                                     Object bodyObject) throws IOException
    {
        return doPut(url, null, headers, bodyObject);
    }
    // endregion

    // region DELETE

    /**
     * 发送 DELETE 请求（无请求体）
     */
    public static HttpResponse doDelete(String url,
                                        Map<String, String> queryParams,
                                        Map<String, String> headers)
            throws IOException
    {
        String realUrl = buildUrl(url, queryParams);
        HttpClient client = HttpClientBuilder.create().build();
        HttpDelete request = new HttpDelete(realUrl);
        setHeaders(request, headers);
        return client.execute(request);
    }

    public static HttpResponse doDelete(String url,
                                        Map<String, String> headers) throws IOException
    {
        return doDelete(url, null, headers);
    }
    // endregion


    // region 工具方法

    /**
     * 从 HttpResponse 中解析出 JSON 并映射为指定类型（Class<T>）。
     * 适合常规 POJO 类（如 User、Order 等）。
     *
     * @param response  HttpResponse 对象
     * @param clazz     目标映射的 Class 类型 (例如 User.class)
     * @param <T>       泛型，表示返回值类型
     * @return 反序列化后的对象实例，若响应体为空则返回 null
     * @throws IOException           流读取异常
     * @throws IllegalStateException 若 response 为 null
     * @throws RuntimeException      若 HTTP 状态码不是 2xx
     */
    public static <T> T parseJsonResponse(HttpResponse response, Class<T> clazz) throws IOException {
        if (response == null) {
            throw new IllegalStateException("HttpResponse is null, cannot parse.");
        }

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new RuntimeException("Request failed with status: " + statusCode);
        }

        org.apache.http.HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }

        try (java.io.InputStream in = entity.getContent()) {
            return objectMapper.readValue(in, clazz);
        }
    }


    /**
     * 从 HttpResponse 中解析出 JSON，并映射为复杂泛型类型 (TypeReference<T>)。
     * 适合像 List<User>、Map<String, Order> 这类嵌套的泛型。
     *
     * @param response  HttpResponse 对象
     * @param typeRef   Jackson的 TypeReference，用于表示复杂泛型
     * @param <T>       泛型，表示返回值类型
     * @return 反序列化后的对象，若响应体为空则返回 null
     * @throws IOException           流读取异常
     * @throws IllegalStateException 若 response 为 null
     * @throws RuntimeException      若 HTTP 状态码不是 2xx
     */
    public static <T> T parseJsonResponse(HttpResponse response, TypeReference<T> typeRef) throws IOException
    {
        if (response == null) {
            throw new IllegalStateException("HttpResponse is null, cannot parse.");
        }

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new RuntimeException("Request failed with status: " + statusCode);
        }

        org.apache.http.HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }

        try (java.io.InputStream in = entity.getContent()) {
            return objectMapper.readValue(in, typeRef);
        }
    }


    private static String buildUrl(String url, Map<String, String> queryParams) throws UnsupportedEncodingException {
        if (queryParams == null || queryParams.isEmpty()) {
            return url;
        }

        boolean hasQuestionMark = url.contains("?");
        StringBuilder sb = new StringBuilder(url);

        // 如果url中不存在 '?'
        if (!hasQuestionMark) {
            sb.append("?");
        }
        else {
            // 如果已存在 '?', 则需要加 '&'
            if (!url.endsWith("?") && !url.endsWith("&")) {
                sb.append("&");
            }
        }

        int index = 0;
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (index > 0) {
                sb.append("&");
            }
            String encodedKey = URLEncoder.encode(entry.getKey(), "UTF-8");
            String encodedValue = URLEncoder.encode(entry.getValue(), "UTF-8");
            sb.append(encodedKey).append("=").append(encodedValue);
            index++;
        }
        return sb.toString();
    }

    private static void setHeaders(HttpUriRequest request, Map<String, String> headers) {
        if (headers == null) {
            return;
        }
        for (Map.Entry<String, String> e : headers.entrySet()) {
            request.setHeader(e.getKey(), e.getValue());
        }
    }

    // endregion

}