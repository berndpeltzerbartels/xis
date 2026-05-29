package one.xis.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creates a Mockito spy, registers it in the XIS test context, and injects it into the annotated test field.
 *
 * <p>Use this when the real object should run during the test, but selected interactions still need to be verified or
 * stubbed.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Spy {
}
