package one.xis.boot.test;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables the XIS JUnit Jupiter integration test context for a test class.
 *
 * <p>The extension starts a small XIS test context, scans the configured packages, and supports the field annotations
 * from {@code one.xis.test} such as {@code @Mock}, {@code @Spy}, {@code @Captor}, and {@code @InTestContext}. Use this
 * annotation for page/frontlet integration tests and for component tests that should run with XIS dependency injection
 * instead of manually wiring every dependency.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(XisBootTestExtension.class)
public @interface XisBootTest {

    /**
     * Package names to scan for XIS components used by the test context.
     */
    String[] packages() default {};

    /**
     * Classes whose packages should be scanned for XIS components.
     */
    Class<?>[] packageClasses() default {};
}
