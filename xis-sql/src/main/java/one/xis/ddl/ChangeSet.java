package one.xis.ddl;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import one.xis.context.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a named set of DDL changes.
 * <p>
 * The value is persisted as the stable change-set id. Use {@link #previous()} to
 * chain change sets in execution order.
 * <p>
 * This annotation is also a XIS and Spring component stereotype, so change-set
 * classes do not need an additional component annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Qualifier
@Singleton
@org.springframework.stereotype.Component
@Component
public @interface ChangeSet {
    String value();

    Class<?> previous() default None.class;

    final class None {
        private None() {
        }
    }
}
