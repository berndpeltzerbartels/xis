package one.xis.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Calls a database stored procedure through a repository proxy method.
 * <p>
 * A stored procedure may declare at most one OUT parameter. XIS treats that OUT
 * parameter as the Java method return value.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StoredProcedure {
    /**
     * @return database procedure name
     */
    String value();

    /**
     * @return optional OUT parameter name. The OUT parameter is appended after all IN parameters.
     */
    String out() default "";
}
