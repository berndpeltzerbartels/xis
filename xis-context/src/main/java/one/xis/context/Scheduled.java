package one.xis.context;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Marks a no-argument component method for scheduled execution by the XIS context.
 * <p>
 * Exactly one of {@link #fixedRate()} and {@link #fixedDelay()} must be greater than {@code 0}.
 * Fixed rate measures the interval between method starts. Fixed delay measures the interval from the end of one
 * invocation to the start of the next.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scheduled {

    /**
     * Delay before the first invocation, measured in {@link #timeUnit()}.
     */
    long initialDelay() default 0;

    /**
     * Interval between invocation starts. Mutually exclusive with {@link #fixedDelay()}.
     */
    long fixedRate() default -1;

    /**
     * Delay after an invocation completes. Mutually exclusive with {@link #fixedRate()}.
     */
    long fixedDelay() default -1;

    /**
     * Unit used for {@link #initialDelay()}, {@link #fixedRate()}, and {@link #fixedDelay()}.
     */
    TimeUnit timeUnit();
}
