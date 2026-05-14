package one.xis.context;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a no-argument component method for scheduled execution by the XIS context.
 * <p>
 * Exactly one of {@link #fixedRateMillis()} and {@link #fixedDelayMillis()} must be greater than {@code 0}.
 * Fixed rate measures the interval between method starts. Fixed delay measures the interval from the end of one
 * invocation to the start of the next.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scheduled {

    /**
     * Delay before the first invocation, in milliseconds.
     */
    long initialDelay() default 0;

    /**
     * Interval between invocation starts, in milliseconds. Mutually exclusive with {@link #fixedDelayMillis()}.
     */
    long fixedRateMillis() default -1;

    /**
     * Delay after an invocation completes, in milliseconds. Mutually exclusive with {@link #fixedRateMillis()}.
     */
    long fixedDelayMillis() default -1;
}
