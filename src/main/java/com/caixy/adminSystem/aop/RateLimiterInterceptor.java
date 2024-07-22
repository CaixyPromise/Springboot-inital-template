package com.caixy.adminSystem.aop;

import com.caixy.adminSystem.annotation.RateLimitFlow;
import com.caixy.adminSystem.common.ErrorCode;
import com.caixy.adminSystem.exception.BusinessException;
import com.caixy.adminSystem.manager.limiter.RateLimiterBuilder;
import com.caixy.adminSystem.manager.limiter.RedisLimiterManager;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 限流器注解切面
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.aop.RateLimiterInterceptor
 * @since 2024-07-17 03:13
 **/
@Aspect
@Component
public class RateLimiterInterceptor
{
    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Pointcut("@annotation(rateLimitFlow)")
    public void rateLimitedMethods(RateLimitFlow rateLimitFlow) {}

    @Around(value = "rateLimitedMethods(rateLimitedFlow)", argNames = "joinPoint,rateLimitedFlow")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimitFlow rateLimitedFlow)
    {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        String dynamicKey = parseKey(rateLimitedFlow.args(), methodSignature, args);

        RateLimiterBuilder<Object> builder = redisLimiterManager.doLimit(rateLimitedFlow.key(), dynamicKey);
        String errorMsg = StringUtils.isNotBlank(rateLimitedFlow.errorMessage()) ? rateLimitedFlow.errorMessage()
                                                                                 : "操作过于频繁，请稍后再试";
        return builder.onSuccess(() ->
                      {
                          try
                          {
                              return joinPoint.proceed();
                          }
                          catch (Throwable throwable)
                          {
                              throw new RuntimeException(throwable);
                          }
                      })
                      .orElseThrow(() -> new BusinessException(ErrorCode.OPERATION_ERROR, errorMsg))
                      .execute();
    }

    private String parseKey(String keyExpression, MethodSignature methodSignature, Object[] args)
    {
        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();
        String[] paramNames = methodSignature.getParameterNames();
        for (int i = 0; i < paramNames.length; i++)
        {
            context.setVariable(paramNames[i], args[i]);
        }
        return parser.parseExpression(keyExpression).getValue(context, String.class);
    }
}
