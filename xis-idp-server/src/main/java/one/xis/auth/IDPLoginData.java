package one.xis.auth;


import one.xis.validation.Validate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Internal validation marker for the built-in XIS OpenID Connect provider login
 * form.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Validate(validatorClass = IDPLoginController.class)
public @interface IDPLoginData {
}
