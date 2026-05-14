package one.xis.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excludes a property from SQL column mapping.
 * <p>
 * Use this annotation for values that belong to the Java object but must never be
 * read from or written to SQL columns, even when a matching column name exists.
 * This is useful for derived values, UI state, or properties handled by custom
 * repository code. Java {@code transient} fields are ignored as well.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
public @interface NoColumn {
}
