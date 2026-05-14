package one.xis.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a repository method as a delete operation.
 * <p>
 * Without an explicit SQL statement, {@code @Delete} deletes one entity instance.
 * The method must then have exactly one parameter whose type is annotated with
 * {@link Entity}. The entity table must have a database primary key. Collection
 * relations are resolved through database foreign-key metadata and are deleted
 * from the leaves toward the root before the entity row is deleted.
 * <p>
 * If the database declares {@code ON DELETE CASCADE} for a direct child relation,
 * XIS does not send an additional delete for that direct child table. It still
 * evaluates deeper child relations first, because those deeper foreign keys may
 * not cascade.
 * <p>
 * With an explicit SQL statement, {@code @Delete} behaves like a normal modifying
 * statement and does not inspect entity metadata:
 * {@code @Delete("delete from people where id = {id}")}. Parameters are bound
 * like in {@link Select}: either positionally through JDBC {@code ?} placeholders
 * or by named placeholders combined with {@link Param}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Delete {
    /**
     * @return optional SQL delete statement. Leave empty for entity delete.
     */
    String value() default "";
}
