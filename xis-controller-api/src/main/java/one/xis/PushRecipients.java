package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for method parameters that are used to specify the recipients of a push message.
 * The parameter must be a Collection or Array of Strings.
 */
@Target(ElementType.PARAMETER)
@Retention(RUNTIME)
// TODO
public @interface PushRecipients {
}
