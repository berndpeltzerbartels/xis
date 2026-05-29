package one.xis.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers the field value as a real object in the XIS test context and injects it into the test field.
 *
 * <p>This is useful for explicitly supplied collaborators that should be available for constructor injection in tested
 * components, but should not be mocked.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InTestContext {
}
