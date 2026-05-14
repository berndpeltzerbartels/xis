package one.xis.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a mapped property as optional for SQL column mapping.
 * <p>
 * A property annotated with {@code @OptionalColumn} is still mapped when the
 * matching column exists. If the column is missing from a result set or table
 * metadata, XIS leaves the property unchanged while mapping rows and omits it
 * from generated {@link Insert}, {@link Update}, and {@link Save} statements.
 * <p>
 * This is mainly intended for reusable or generic entities whose consumers may
 * choose not to persist every inherited property. It should not be used to hide
 * ordinary spelling mistakes in column names; strict mapping remains the default.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
public @interface OptionalColumn {
}
