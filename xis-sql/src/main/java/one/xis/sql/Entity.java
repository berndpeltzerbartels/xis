package one.xis.sql;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a mapped class or record as a database entity and provides the existing table name.
 * <p>
 * The SQL mapper uses this table name to resolve and validate relations through database
 * foreign-key metadata. This annotation is not used to create or alter tables.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
    /**
     * @return existing database table name
     */
    String value();

    /**
     * Allows entity fields without matching result columns to stay unmapped.
     * <p>
     * The default is strict because a missing column usually means a broken SQL statement
     * or a forgotten {@link Column} annotation. Enable this only for adapter entities that
     * intentionally inherit fields which are not persisted by this table.
     *
     * @return true if unmapped fields should be ignored for result mapping
     */
    boolean allowUnmappedFields() default false;
}
