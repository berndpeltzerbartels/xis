package one.xis.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a SQL select statement for a repository proxy method.
 * <p>
 * Parameters are bound positionally to JDBC {@code ?} placeholders. Result rows are mapped
 * by the SQL mapper to a single object, {@link java.util.Optional}, or a {@link java.util.List},
 * depending on the method return type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Select {
    /**
     * @return SQL select statement
     */
    String value();
}
