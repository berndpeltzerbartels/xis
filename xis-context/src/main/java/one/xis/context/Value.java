package one.xis.context;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects a configuration property into a component field.
 *
 * <p>The value is the property key. When {@link #mandatory()} is {@code true}, context startup fails if the property is
 * missing.</p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {
    /**
     * Configuration property key.
     */
    String value();

    /**
     * Whether the property must exist.
     */
    boolean mandatory() default true;
}
