package com.caixy.adminSystem.utils;

import org.apache.poi.ss.formula.functions.T;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Servlet工具类
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.utils.SeveletUtils
 * @since 2024/10/10 下午9:58
 */
public class ServletUtils
{
    /**
     * 获取当前会话的 Session ID
     *
     * @param request HttpServletRequest 请求对象
     * @return 返回会话的唯一 Session ID
     */
    public static Optional<String> getSessionId(HttpServletRequest request)
    {
        return Optional.ofNullable(request.getSession().getId());
    }

    /**
     * 从当前会话的 Session 中获取属性
     *
     * @param <T>     属性的类型
     * @param key     属性的键名
     * @param request HttpServletRequest 请求对象
     * @return 返回对应键名的属性值，如果没有找到则返回 Optional.empty()
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getAttributeFromSession(String key, HttpServletRequest request)
    {
        return Optional.ofNullable((T) request.getSession().getAttribute(key));
    }

    /**
     * 判断当前会话的 Session 中是否存在指定属性
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/10 下午10:42
     */
    public static Boolean hasAttributeInSession(String key, HttpServletRequest request)
    {
        return request.getSession().getAttribute(key) != null;
    }

    /**
     * 设置属性到当前会话的 Session 中
     *
     * @param key     属性的键名
     * @param value   属性的值
     * @param request HttpServletRequest 请求对象
     */
    public static void setAttributeInSession(String key, Object value, HttpServletRequest request)
    {
        request.getSession().setAttribute(key, value);
    }

    /**
     * 从当前会话的 Session 中移除属性
     *
     * @param key     要移除的属性键名
     * @param request HttpServletRequest 请求对象
     */
    public static void removeAttributeInSession(String key, HttpServletRequest request)
    {
        request.getSession().removeAttribute(key);
    }

    /**
     * 使当前会话失效
     *
     * @param request HttpServletRequest 请求对象
     */
    public static void invalidate(HttpServletRequest request)
    {
        request.getSession().invalidate();
    }

    /**
     * 从请求头部中获取指定属性
     *
     * @param key     请求头属性的键名
     * @param request HttpServletRequest 请求对象
     * @return 返回对应键名的请求头部属性值，如果没有找到则返回 Optional.empty()
     */
    public static Optional<String> getAttributeFromHeader(String key, HttpServletRequest request)
    {
        return Optional.ofNullable(request.getHeader(key));
    }

    /**
     * 设置自定义响应头到 HttpServletResponse 中
     *
     * @param key      响应头属性的键名
     * @param value    响应头属性的值
     * @param response HttpServletResponse 响应对象
     */
    public static void setAttributeInHeader(String key, String value, HttpServletResponse response)
    {
        response.setHeader(key, value);
    }

    /**
     * 移除响应头部的自定义属性
     *
     * @param key      响应头属性的键名
     * @param response HttpServletResponse 响应对象
     */
    public static void removeAttributeInHeader(String key, HttpServletResponse response)
    {
        response.setHeader(key, null);
    }

    /**
     * 设置响应的缓存控制头
     *
     * @param response     HttpServletResponse 响应对象
     * @param cacheControl 缓存控制的值，例如 "no-cache", "max-age=3600" 等
     */
    public static void setCacheControlHeader(HttpServletResponse response, String cacheControl)
    {
        response.setHeader("Cache-Control", cacheControl);
    }

    /**
     * 设置响应的重定向地址
     *
     * @param response HttpServletResponse 响应对象
     * @param location 重定向的 URL
     * @throws IOException 如果发生 I/O 异常
     */
    public static void sendRedirect(HttpServletResponse response, String location) throws IOException
    {
        response.sendRedirect(location);
    }

    /**
     * 设置 Content-Type 响应头
     *
     * @param response    HttpServletResponse 响应对象
     * @param contentType 响应内容类型，例如 "application/json", "text/html" 等
     */
    public static void setContentType(HttpServletResponse response, String contentType)
    {
        response.setContentType(contentType);
    }

    /**
     * 设置跨域访问的响应头
     *
     * @param response    HttpServletResponse 响应对象
     * @param allowOrigin 允许的域名
     */
    public static void setCORSHeaders(HttpServletResponse response, String allowOrigin)
    {
        response.setHeader("Access-Control-Allow-Origin", allowOrigin);
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }
}
