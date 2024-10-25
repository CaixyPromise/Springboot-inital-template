package com.caixy.adminSystem.config;

import com.caixy.adminSystem.filter.repeat.RepeatableFilter;
import com.caixy.adminSystem.filter.xss.XssFilter;
import com.caixy.adminSystem.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.DispatcherType;
import java.util.HashMap;
import java.util.Map;

/**
 * Xss过滤器配置
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.config.XssFilterConfig
 * @since 2024/10/20 01:28
 */
@Configuration
public class XssFilterConfig
{
    @Value("${xss.excludes}")
    private String excludes;

    @Value("${xss.urlPatterns}")
    private String urlPatterns;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Bean
    @ConditionalOnProperty(value = "xss.enabled", havingValue = "true")
    public FilterRegistrationBean xssFilterRegistration()
    {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new XssFilter());
        //添加过滤路径
        registration.addUrlPatterns(StringUtils.split(urlPatterns, ","));
        registration.setName("xssFilter");
        registration.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);
        // 设置初始化参数
        Map<String, String> initParameters = new HashMap<>();
        // 处理 excludes，动态添加 context-path
        initParameters.put("excludes", addContextPathToExcludes(excludes, contextPath));
        registration.setInitParameters(initParameters);

        return registration;
    }

    /**
     * 自动将 context-path 添加到每个排除路径中
     * 如果 context-path 存在（且不为 /），addContextPathToExcludes 会自动为每个排除路径加上 context-path。
     * 如果 context-path 不存在或为根路径 /，这个方法也会处理为不加 context-path。
     *
     * @param excludes    排除路径
     * @param contextPath context-path
     * @return 添加 context-path 后的排除路径
     */
    private String addContextPathToExcludes(String excludes, String contextPath)
    {
        if (StringUtils.isEmpty(contextPath) || "/".equals(contextPath))
        {
            return excludes;
        }
        // 将 context-path 添加到每个排除路径中
        String[] paths = StringUtils.split(excludes, ",");
        StringBuilder result = new StringBuilder();
        for (String path : paths)
        {
            result.append(contextPath).append(path).append(",");
        }
        return result.toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Bean
    public FilterRegistrationBean someFilterRegistration()
    {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new RepeatableFilter());
        registration.addUrlPatterns("/*");
        registration.setName("repeatableFilter");
        registration.setOrder(FilterRegistrationBean.LOWEST_PRECEDENCE);
        return registration;
    }
}
