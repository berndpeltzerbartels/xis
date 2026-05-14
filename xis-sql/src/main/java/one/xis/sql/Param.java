package one.xis.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Names a repository method parameter for SQL statements with named placeholders.
 * <p>
 * A placeholder like {@code {id}} in a {@link Select} statement is replaced with
 * a JDBC {@code ?} placeholder and bound to the method parameter annotated with
 * {@code @Param("id")}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {
    /**
     * @return placeholder name used in the SQL statement without braces
     */
    String value();
}
