package one.xis.sql;

import one.xis.context.Proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface as a SQL repository.
 * <p>
 * Repository interfaces may declare SQL methods with {@link Select}, {@link Insert},
 * {@link Update}, {@link Save}, and {@link Delete}. They may also extend
 * {@link CrudRepository} to inherit generic CRUD operations for entities whose
 * table has exactly one primary-key column.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Proxy(factory = SQLRepositoryProxyFactory.class)
public @interface Repository {
}
