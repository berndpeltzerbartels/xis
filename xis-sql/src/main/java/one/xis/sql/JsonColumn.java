package one.xis.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Stores a property as JSON in a single database column.
 * <p>
 * Use this for small value objects or collections of values that are part of the
 * owning entity and should not become their own SQL relation. Types annotated
 * with {@link Entity} are still treated as relations unless the property itself
 * is explicitly annotated with {@code @JsonColumn}.
 * <p>
 * {@code @JsonColumn} can be combined with {@link OptionalColumn} when reusable
 * entities expose a property that some applications persist and others load by
 * custom repository code.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
public @interface JsonColumn {
}
