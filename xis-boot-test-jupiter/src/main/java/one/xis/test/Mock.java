package one.xis.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creates a Mockito mock, registers it in the XIS test context, and injects it into the annotated test field.
 *
 * <p>Use this when a component under test should receive a mocked collaborator through XIS dependency injection.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Mock {
}
