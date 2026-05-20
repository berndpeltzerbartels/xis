package one.xis;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Runs an application-defined ownership check for a submitted object.
 * <p>
 * The annotated value is already deserialized when XIS calls the configured {@link OwnershipGuard}. XIS provides the
 * trusted {@link UserContext}; the guard owns the application-specific decision whether the current user may access the
 * submitted object.
 */
@Retention(RUNTIME)
@Target({TYPE, PARAMETER, FIELD, RECORD_COMPONENT})
public @interface OwnedBy {
    Class<? extends OwnershipGuard<?>> value();
}
