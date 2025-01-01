package com.caixy.adminSystem.infrastructure.lock.aop;

import com.caixy.adminSystem.common.base.utils.StringUtils;
import com.caixy.adminSystem.infrastructure.lock.manager.DistributedLockManager;
import com.caixy.adminSystem.infrastructure.lock.annotation.DistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 分布式锁切面方法
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.manager.RDLock.aop.DistributedLockInterceptor
 * @since 2024-07-20 01:55
 **/
@Aspect
@Component
public class DistributedLockInterceptor
{
    @Resource
    private DistributedLockManager distributedLockManager;

    private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

    @Around("@annotation(distributedLock)")
    public Object lockMethod(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable
    {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        String dynamicKey = null;
        if (StringUtils.isBlank(distributedLock.args()))
        {
            dynamicKey = parseKey(distributedLock.args(), signature, args);
        }
        return distributedLockManager.redissonDistributedLocks(
                distributedLock.lockKeyEnum(),
                () -> {
                    try {
                        return joinPoint.proceed();
                    }
                    catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                },
                dynamicKey);
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
