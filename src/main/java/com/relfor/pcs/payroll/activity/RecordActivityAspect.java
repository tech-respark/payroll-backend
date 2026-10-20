package com.relfor.pcs.payroll.activity;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RecordActivityAspect {

    private final ActivityRecorder activityRecorder;

    public RecordActivityAspect(ActivityRecorder activityRecorder) {
        this.activityRecorder = activityRecorder;
    }

    @Around("@annotation(recordActivity)")
    public Object around(ProceedingJoinPoint joinPoint, RecordActivity recordActivity) throws Throwable {
        Object result = joinPoint.proceed();
        ActivityEvent.Builder builder = ActivityEvent.builder()
                .action(recordActivity.action())
                .module(recordActivity.module())
                .summary(recordActivity.summary().isBlank() ? recordActivity.action() : recordActivity.summary());
        if (!recordActivity.resourceType().isBlank()) {
            builder.resourceType(recordActivity.resourceType());
        }
        if (!recordActivity.resourceIdParam().isBlank()) {
            Object resourceId = argument(joinPoint, recordActivity.resourceIdParam());
            builder.resourceId(resourceId);
        }
        activityRecorder.record(builder.build());
        return result;
    }

    private static Object argument(ProceedingJoinPoint joinPoint, String name) {
        if (!(joinPoint.getSignature() instanceof CodeSignature signature)) {
            return null;
        }
        String[] names = signature.getParameterNames();
        Object[] values = joinPoint.getArgs();
        if (names == null) {
            return null;
        }
        for (int i = 0; i < names.length && i < values.length; i++) {
            if (name.equals(names[i])) {
                return values[i];
            }
        }
        return null;
    }
}
