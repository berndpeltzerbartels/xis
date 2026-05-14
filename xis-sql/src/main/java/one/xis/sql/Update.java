package one.xis.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a SQL update statement for a repository proxy method.
 * <p>
 * Parameters are bound like in {@link Select}: either positionally through JDBC
 * {@code ?} placeholders or by named placeholders such as {@code {id}} combined
 * with {@link Param}. If the method has one unannotated entity parameter, named
 * placeholders may also refer directly to entity properties.
 * <p>
 * If no SQL statement is supplied, the method must have exactly one entity
 * parameter. XIS updates all non-primary-key columns and uses all primary-key
 * columns in the {@code where} clause.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Update {
    /**
     * @return optional SQL update statement. Leave empty to update one entity parameter
     * using database metadata and the table primary key.
     */
    String value() default "";
}
