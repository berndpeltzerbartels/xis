package one.xis.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrides the database column name for a mapped field or record component.
 * <p>
 * This annotation only describes an existing column name. It is not used for DDL
 * generation and deliberately has no attributes for SQL type, length, nullability,
 * or constraints.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface Column {
    /**
     * @return existing database column name
     */
    String value();
}
