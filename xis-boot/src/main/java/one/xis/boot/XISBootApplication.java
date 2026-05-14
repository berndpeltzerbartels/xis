package one.xis.boot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Marks the entry point of a standalone XIS Boot application.
 * <p>
 * The main purpose of this annotation is build-time generation for the XIS
 * Gradle plugin's {@code xisJar} task. The XIS annotation processor uses
 * the annotated class to generate {@code one.xis.boot.Runner}, which becomes
 * the executable jar's {@code Main-Class} and delegates to
 * {@link XISBootRunner}.
 */
@Target(TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface XISBootApplication {
}
