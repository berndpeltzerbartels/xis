package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a value from global variables to a method parameter,
 * or stores a method's return value in global variables.
 * 
 * Global variables are temporary and exist only during the processing
 * of a single request. They are cleared at the end of each request
 * and allow sharing data between widgets during that request.
 *
 * <p><strong>Usage on method parameters:</strong><br>
 * The value associated with the given key will be read from global variables
 * and passed into the method as an argument.
 *
 * <pre>{@code
 * public String render(@GlobalVariable("sharedData") String data) {
 *     return "Shared: " + data;
 * }
 * }</pre>
 *
 * <p><strong>Usage on methods:</strong><br>
 * The method's return value will be stored in global variables
 * with the specified key.
 *
 * <pre>{@code
 * @GlobalVariable("sharedData")
 * public String calculateData() {
 *     return "Some calculated value";
 * }
 * }</pre>
 *
 * <p><strong>Template usage:</strong><br>
 * Global variables can be accessed in templates using the {@code global.} prefix:
 * <pre>{@code
 * <span>${global.sharedData}</span>
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalVariable {
    String value();
}