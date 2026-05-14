package one.xis.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Calls a database function through a repository proxy method.
 * <p>
 * The Java method return value is the function return value. Method parameters
 * are IN parameters and are bound in method order.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Function {
    /**
     * @return database function name
     */
    String value();
}
