package one.xis.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excludes a field, accessor method, or record component from SQL mapping.
 * <p>
 * The result-to-object mapper is strict for mapped properties: every non-ignored
 * property must be backed by a result column or relation. Use this annotation for
 * derived or application-only values. Java {@code transient} fields are ignored as well.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
public @interface Ignore {
}
