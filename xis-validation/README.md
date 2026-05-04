# XIS Validation

This module contains the validation annotations, validator extension points, message resolution, and default validation
messages used by XIS when request, form, action, and model values are deserialized.

`xis-validation` is not a primary runtime module, but it is part of the public programming model. Applications usually
receive it transitively through the selected runtime:

- `xis-boot`
- `xis-spring`

Most users therefore do not need to add this module explicitly. They should depend on one runtime module and import the
validation annotations from `one.xis.validation`.

## Dependency Model

Use one runtime dependency in an application:

```groovy
dependencies {
    implementation("one.xis:xis-boot:<version>")
}
```

or:

```groovy
dependencies {
    implementation("one.xis:xis-spring:<version>")
}
```

Both runtime modules expose `xis-validation` transitively, so application classes can use validation annotations such as
`@EMail`, `@MinLength`, `@RegExpr`, and `@LabelKey` without declaring `xis-validation` separately.

Add `xis-validation` directly only when you intentionally build a library that should compile against the XIS validation
annotations without selecting a runtime.

## Built-In Validation Annotations

| Annotation | Target | Purpose |
| --- | --- | --- |
| `@EMail` | field, parameter, record component | Validates that a string looks like an email address. |
| `@MinLength` | field, parameter, record component | Validates the minimum length or size of strings, collections, arrays, or maps. |
| `@RegExpr` | field, parameter, record component | Validates a string against a regular expression. |
| `@LabelKey` | field, parameter, record component, type | Resolves a user-facing label for validation messages. |

`@Mandatory` and `@AllElementsMandatory` are also validation-related annotations, but they live in
[`xis-controller-api`](../xis-controller-api/README.md) because the deserialization layer needs them as part of the
core controller binding contract.

## Custom Validators

Custom validation annotations are built by annotating another annotation with `@Validate`.

```java
package com.example;

import one.xis.validation.Validate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Validate(validatorClass = SkuValidator.class, messageKey = "validation.sku")
public @interface Sku {
}
```

The validator implements `Validator<T>`:

```java
package com.example;

import one.xis.UserContext;
import one.xis.validation.Validator;
import one.xis.validation.ValidatorException;

import java.lang.reflect.AnnotatedElement;

public class SkuValidator implements Validator<String> {

    @Override
    public void validate(String value, AnnotatedElement target, UserContext userContext) throws ValidatorException {
        if (!value.matches("[A-Z]{3}-[0-9]{4}")) {
            throw new ValidatorException();
        }
    }
}
```

Validators can also throw `ValidatorException` with message parameters. Those parameters are available to the validation
message template.

## Validation Messages

Validation messages are resolved from properties files on the classpath.

Application files can override or extend the default messages:

- `messages.properties`
- `messages_de.properties`
- `messages_<language>.properties`

XIS also ships default messages:

- `default-messages.properties`
- `default-messages_en.properties`
- `default-messages_de.properties`
- `default-messages_fr.properties`
- `default-messages_es.properties`
- `default-messages_pl.properties`

For a request locale such as Polish, messages are resolved in this order:

1. `messages_pl.properties`
2. `default-messages_pl.properties`
3. `messages.properties`
4. `default-messages.properties`

The first matching key wins. Application `messages*.properties` files are the normal extension point; the
`default-messages*.properties` files are framework fallbacks.

`@LabelKey` can point to a label entry that is substituted into validation messages via the `${label}` placeholder.

```java
public record UserForm(
        @EMail
        @LabelKey("user.email")
        String email
) {
}
```

```properties
user.email=Email address
validation.email=The ${label} is not a valid email address.
```

## Minimal Example

```java
package com.example;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;
import one.xis.validation.EMail;
import one.xis.validation.LabelKey;
import one.xis.validation.MinLength;

@Page("/register.html")
public class RegisterPage {

    @Action
    public void register(@FormData("user") UserForm user) {
        userService.register(user);
    }

    public record UserForm(
            @EMail
            @LabelKey("user.email")
            String email,

            @MinLength(8)
            @LabelKey("user.password")
            String password
    ) {
    }
}
```

During deserialization, XIS validates annotated fields, parameters, and record components before the controller method
continues with the bound value.
