package one.xis.context;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method for scheduled execution by the XIS context.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scheduled { // TODO
    long initialDelay() default 0;

    long fixedRateMillis() default -1;

    long fixedDelayMillis() default -1;
}
