package one.xis;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import one.xis.context.XISComponent;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Qualifier // for micronaut
@Singleton // for micronaut
@XISComponent
public @interface Widget {
    String value() default "";
}
