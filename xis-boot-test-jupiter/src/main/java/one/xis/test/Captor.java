package one.xis.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creates and injects a Mockito argument captor for a test field.
 *
 * <p>Use this together with {@code @XisBootTest} when a test needs to verify values passed to a mocked or spied
 * dependency.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Captor {
}
