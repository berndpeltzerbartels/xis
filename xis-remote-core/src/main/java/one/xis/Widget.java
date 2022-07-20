package one.xis;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@Qualifier // for micronaut
@Singleton // for micronaut
@Component // for spring
public @interface Widget {
    String value() default ""; // an alias
}
