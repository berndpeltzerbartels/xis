package one.xis.http;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A controller class annotated with @PublicResourcePaths indicates that it serves static resources
 * from the specified paths without requiring authentication.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PublicResources {
    String[] value();
}
