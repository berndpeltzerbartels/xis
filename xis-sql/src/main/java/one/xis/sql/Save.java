package one.xis.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Saves one entity instance by using database metadata to find the table
 * primary key and foreign keys.
 * <p>
 * The method must have exactly one entity parameter. The mapper checks whether a row
 * with the entity primary-key value exists and then performs an insert or update.
 * Tables without primary-key metadata are rejected during handler initialization.
 * <p>
 * Single entity references are stored through a foreign-key column on the saved
 * entity table. Collection fields are treated as child rows: the parent row is
 * saved first, then every collection element is inserted or updated and receives
 * the parent's primary-key value in its foreign-key column. Removed collection
 * elements are not deleted by {@code @Save}; use {@link Delete} or explicit SQL
 * for delete semantics.
 * <p>
 * If a SQL statement is supplied, {@code @Save} behaves like a normal
 * modification statement. In that mode it does not inspect relation metadata and
 * does not cascade.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Save {
    /**
     * @return optional SQL statement. Leave empty to save one entity parameter
     * using database metadata.
     */
    String value() default "";
}
