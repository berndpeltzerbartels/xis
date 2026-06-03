package one.xis.ddl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks one DDL migration method inside a {@link ChangeSet}.
 * <p>
 * The value is persisted as the stable change id inside its change set.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Change {
    String value();
}
