package one.xis;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import one.xis.context.Component;

import java.lang.annotation.*;

/**
 * Marks a class as a Javascript extension.
 * The value is the classpath resource path to the javascript file.
 * The content of the file will be loaded and added to the generated javascript for the application.
 * This is because in a single page application all javascript must be loaded at once and can not be loaded on demand.
 * <p>
 * Classes annotated with @JavascriptExtension can not be interfaces or abstract classes.
 */
@Qualifier // for micronaut
@Singleton // for micronaut
@org.springframework.stereotype.Component // for spring
@Component
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JavascriptExtension {
    String value();
}
