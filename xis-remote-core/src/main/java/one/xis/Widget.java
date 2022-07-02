package one.xis;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@Qualifier // for micronaut
@Singleton
public @interface Widget {
}
