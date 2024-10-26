package com.caixy.adminSystem.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
     * @return 返回会话的唯一 Session ID
     */
    public static String getSessionId()
    {
        return getRequest().getRequestedSessionId();
    }

    /**
     * 获取当前请求的 session 对象
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/20 上午1:19
     */
    public static HttpSession getSession()
    {
        HttpServletRequest request = getRequest();
        return request.getSession();
    }

    /**
     * 从当前会话的 Session 中获取属性
     *
     * @param <T> 属性的类型
     * @param key 属性的键名
     * @return 返回对应键名的属性值，如果没有找到则返回 Optional.empty()
     */
    public static <T> Optional<T> getAttributeFromSession(String key, Class<T> clazz)
    {
        Object attribute = getSession().getAttribute(key);
        return Optional.ofNullable(clazz.isInstance(attribute) ? clazz.cast(attribute) : null);
    }


    /**
     * 获取会话中的属性，若不存在则返回 null
     *
     * @param key   会话属性的键
     * @param clazz 属性的类型
     * @param <T>   返回值的类型
     * @return 会话属性的值，若不存在则返回 null
     */
    public static <T> T getAttributeFromSessionOrNull(String key, Class<T> clazz)
    {
        return getAttributeFromSession(key, clazz).orElse(null);
    }


    /**
     * 判断当前会话的 Session 中是否存在指定属性
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/10 下午10:42
     */
    public static Boolean hasAttributeInSession(String key)
    {
        return getSession().getAttribute(key) != null;
    }

    /**
     * 设置属性到当前会话的 Session 中
     *
     * @param key   属性的键名
     * @param value 属性的值
     */
    public static void setAttributeInSession(String key, Object value)
    {
        getSession().setAttribute(key, value);
    }

    /**
     * 从当前会话的 Session 中移除属性
     *
     * @param key 要移除的属性键名
     */
    public static void removeAttributeInSession(String key)
    {
        getSession().removeAttribute(key);
    }

    /**
     * 获取当前请求的 ServletRequestAttributes 对象，可以从中获得 HttpServletRequest/HttpServletResponse。
     * <p>在以下情况中，可能会无法获取到请求属性（返回 null）：</p>
     * <ol>
     * <li>在非 Web 环境下运行，例如在独立的后台任务中。</li>
     * <li>在异步操作或多线程环境中运行，因没有共享的请求上下文。</li>
     * <li>在没有活动请求上下文的情况下调用，例如非请求生命周期内调用。</li>
     * <li>在测试环境中（如单元测试或集成测试）未正确模拟请求。</li>
     * <li>在 Spring 配置中关闭了请求作用域 (RequestScope)。</li>
     * <li>在 WebSocket 或其他非标准 HTTP 请求中。</li>
     * </ol>
     *
     * @return 当前请求的 ServletRequestAttributes，如果没有请求上下文则返回 null
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/20 上午1:16
     */
    @NotNull
    public static Optional<ServletRequestAttributes> getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes
               ? Optional.of((ServletRequestAttributes) attributes)
               : Optional.empty();
    }


    /**
     * 获取当前请求的 HttpServletRequest 对象
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/20 上午1:20
     */
    public static HttpServletRequest getRequest()
    {
        return getRequestAttributes()
                .map(ServletRequestAttributes::getRequest)
                .orElseThrow(() -> new RuntimeException("无法检查到 Request 上下文"));
    }

    /**
     * 获取当前请求的 HttpServletResponse 对象
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/10/27 上午12:22
     */
    public static HttpServletResponse getResponse()
    {
        return getRequestAttributes()
                .map(ServletRequestAttributes::getResponse)
                .orElseThrow(() -> new RuntimeException("无法检查到 Request 上下文"));
    }

    /**
     * 使当前会话失效
     */
    public static void invalidate()
    {
        invalidate(getSession());
    }

    /**
     * 使当前会话失效
     */
    public static void invalidate(HttpSession session)
    {
        getSession().invalidate();
    }

    /**
     * 将字符串渲染到客户端
     *
     * @param response 渲染对象
     * @param string   待渲染的字符串
     */
    public static void renderString(HttpServletResponse response, String string)
    {
        try
        {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 从请求头部中获取指定属性
     *
     * @param key     请求头属性的键名
     * @param request HttpServletRequest 请求对象
     * @return 返回对应键名的请求头部属性值，如果没有找到则返回 Optional.empty()
     */
    public static Optional<String> getAttributeFromHeader(String key)
    {
        return Optional.ofNullable(getRequest().getHeader(key));
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
