# Forms and Validation

XIS binds form data to Java objects with `@FormData`. Actions receive typed form objects instead of manually parsing
request parameters.

## Basic Form Binding

```java
package example.users;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;

@Page("/users/new.html")
public class NewUserPage {

    @FormData("user")
    public UserForm user() {
        return new UserForm("", "");
    }

    @Action
    public Class<?> saveUser(@FormData("user") UserForm user) {
        userService.save(user);
        return UserListPage.class;
    }

    public record UserForm(String firstName, String email) {
    }
}
```

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <meta charset="UTF-8">
    <title>New User</title>
</head>
<body>
    <form xis:binding="user">
        <label>
            First name
            <input type="text" xis:binding="firstName"/>
        </label>

        <label>
            Email
            <input type="email" xis:binding="email"/>
        </label>

        <button type="submit" xis:action="saveUser">Save</button>
    </form>
</body>
</html>
```

The form binding name and `@FormData` name must match.

## Multiple Submit Actions

One form can have multiple actions.

```java
@Action("saveDraft")
public void saveDraft(@FormData("document") DocumentForm document) {
    documentService.saveDraft(document);
}

@Action("publish")
public Class<?> publish(@FormData("document") DocumentForm document) {
    documentService.publish(document);
    return PublishedDocumentsPage.class;
}
```

```html
<form xis:binding="document">
    <input type="text" xis:binding="title"/>
    <textarea xis:binding="content"></textarea>

    <button type="submit" xis:action="saveDraft">Save draft</button>
    <button type="submit" xis:action="publish">Publish</button>
</form>
```

Element syntax:

```html
<xis:form binding="document">
    <xis:input type="text" binding="title"/>
    <xis:textarea binding="content"/>

    <xis:submit action="saveDraft">Save draft</xis:submit>
    <xis:submit action="publish">Publish</xis:submit>
</xis:form>
```

## Nested Objects and Lists

Use dot notation for nested objects:

```html
<input type="text" xis:binding="customer.name"/>
<input type="email" xis:binding="customer.email"/>
<input type="text" xis:binding="shippingAddress.street"/>
```

Use indexed notation for lists:

```html
<div xis:foreach="item:${cart.items}">
    <input type="hidden" xis:binding="items[${itemIndex}].productId"/>
    <input type="number" xis:binding="items[${itemIndex}].quantity"/>
</div>
```

Use repeated bindings when several controls should contribute values to the same Java property. This is common for
checkbox groups:

```java
public class ProductForm {
    private List<String> tags;

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
```

```html
<form xis:binding="product">
    <input type="checkbox" xis:binding="tags" value="new"/>
    <input type="checkbox" xis:binding="tags" value="sale"/>
    <input type="checkbox" xis:binding="tags" value="archived"/>

    <button type="submit" xis:action="save">Save</button>
</form>
```

Element syntax:

```html
<xis:form binding="product">
    <xis:checkbox binding="tags" value="new"/>
    <xis:checkbox binding="tags" value="sale"/>
    <xis:checkbox binding="tags" value="archived"/>

    <xis:submit action="save">Save</xis:submit>
</xis:form>
```

If the user selects `new` and `sale`, the action receives both values in `ProductForm.tags`.

## Validation

XIS validates submitted form data before the action method executes. If validation fails, the action is not called and
validation messages are available to the template.

```java
package example.users;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;
import one.xis.validation.EMail;
import one.xis.validation.LabelKey;
import one.xis.validation.Mandatory;
import one.xis.validation.MinLength;

@Page("/register.html")
public class RegisterPage {

    @FormData("user")
    public UserForm user() {
        return new UserForm("", "");
    }

    @Action
    public void register(@FormData("user") UserForm user) {
        userService.register(user);
    }

    public record UserForm(
            @Mandatory
            @LabelKey("user.email")
            @EMail
            String email,

            @Mandatory
            @LabelKey("user.password")
            @MinLength(8)
            String password
    ) {
    }
}
```

```html
<form xis:binding="user">
    <xis:global-messages/>

    <label>
        Email
        <input type="email" xis:binding="email"/>
    </label>
    <div xis:message-for="email"></div>

    <label>
        Password
        <input type="password" xis:binding="password"/>
    </label>
    <div xis:message-for="password"></div>

    <button type="submit" xis:action="register">Register</button>
</form>
```

Common annotations:

| Annotation | Purpose |
| --- | --- |
| `@Mandatory` | Value must not be null, empty, or blank. |
| `@EMail` | String must be an email address. |
| `@MinLength` | String, collection, array, or map must have a minimum length or size. |
| `@RegExpr` | String must match a regular expression. |
| `@LabelKey` | Supplies a user-facing label for validation messages. |

Validation messages are resolved from message property files on the classpath, such as `messages.properties` and
`messages_de.properties`.

## Custom Validators

Create a custom annotation with `@Validate`.

```java
package example.validation;

import one.xis.validation.Validate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({FIELD, PARAMETER, RECORD_COMPONENT})
@Validate(validatorClass = SkuValidator.class, messageKey = "validation.sku")
public @interface Sku {
}
```

```java
package example.validation;

import one.xis.UserContext;
import one.xis.validation.Validator;
import one.xis.validation.ValidatorException;

import java.lang.reflect.AnnotatedElement;

public class SkuValidator implements Validator<String> {

    @Override
    public void validate(String value, AnnotatedElement target, UserContext userContext) throws ValidatorException {
        if (value == null || !value.matches("[A-Z]{3}-[0-9]{4}")) {
            throw new ValidatorException();
        }
    }
}
```
