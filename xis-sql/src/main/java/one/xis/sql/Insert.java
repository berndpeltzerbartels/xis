package one.xis.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a SQL insert statement for a repository proxy method.
 * <p>
 * Parameters are bound like in {@link Select}: either positionally through JDBC
 * {@code ?} placeholders or by named placeholders such as {@code {name}} combined
 * with {@link Param}. If the method has one unannotated entity parameter, named
 * placeholders may also refer directly to entity properties.
 * <p>
 * If no SQL statement is supplied, the method must have exactly one entity
 * parameter. XIS builds an insert statement from the entity mapping and database
 * metadata.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Insert {
    /**
     * @return optional SQL insert statement. Leave empty to insert one entity parameter
     * using database metadata.
     */
    String value() default "";
}
