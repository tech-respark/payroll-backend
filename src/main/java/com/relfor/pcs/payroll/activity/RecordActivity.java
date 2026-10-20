package com.relfor.pcs.payroll.activity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional convenience for simple mutations. Prefer {@link ActivityRecorder}
 * when the summary or resource identity is known only inside the method body.
 *
 * <p>The event is written after the method returns normally. Thrown exceptions
 * are not recorded as success; record {@link ActivityOutcome#FAILURE} explicitly
 * if a denied or failed attempt must appear in the log.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RecordActivity {

    String action();

    ActivityModule module();

    String resourceType() default "";

    /** Method parameter name whose value becomes {@code resource_id}. */
    String resourceIdParam() default "";

    String summary() default "";
}
