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

## Type Conversion

XIS deserializes submitted form values into the Java types used by the `@FormData` object. Empty strings are treated as
missing values; with `@Mandatory`, that becomes a validation error.

Numbers are parsed in two steps. XIS first accepts the canonical Java/JSON representation, then falls back to the
request locale. That means a German user can enter `1.234,56` for `BigDecimal`, `Double`, or `Float`, while `1234.56`
still works as a canonical value. Integer types also accept locale grouping, such as `1.234` in German or `1,234` in
English, but reject fractional values like `1,5`.

```java
public record ProductForm(
        String name,
        BigDecimal price,
        Integer stock
) {
}
```

```html
<form xis:binding="product">
    <input type="text" xis:binding="name"/>
    <input type="text" xis:binding="price"/>
    <input type="text" xis:binding="stock"/>
    <button type="submit" xis:action="save">Save</button>
</form>
```

Element syntax:

```html
<xis:form binding="product">
    <xis:input type="text" binding="name"/>
    <xis:input type="text" binding="price"/>
    <xis:input type="text" binding="stock"/>
    <xis:submit action="save">Save</xis:submit>
</xis:form>
```

Use a formatter when the displayed string is application-specific, for example money with a currency symbol,
percentages, coordinates, or a date format that differs from the built-in conversion.

```java
import one.xis.Formatter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.ZoneId;
import java.util.Locale;

public class MoneyFormatter implements Formatter<BigDecimal> {

    @Override
    public String format(BigDecimal value, Locale locale, ZoneId zoneId) {
        return NumberFormat.getCurrencyInstance(locale).format(value);
    }

    @Override
    public BigDecimal parse(String text, Locale locale, ZoneId zoneId) {
        var format = NumberFormat.getCurrencyInstance(locale);
        if (format instanceof DecimalFormat decimalFormat) {
            decimalFormat.setParseBigDecimal(true);
        }
        var position = new ParsePosition(0);
        var number = format.parse(text, position);
        if (number == null || position.getIndex() != text.length()) {
            throw new IllegalArgumentException("Invalid money value");
        }
        if (number instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return BigDecimal.valueOf(number.doubleValue());
    }
}
```

```java
import one.xis.UseFormatter;

public record ProductForm(
        @UseFormatter(MoneyFormatter.class)
        BigDecimal price
) {
}
```

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
| `@LabelKey` | Supplies a context-specific label for validation messages. |

Field messages are shown with `xis:message-for`. Global messages are shown with `<xis:global-messages/>`.

```html
<form xis:binding="user">
    <div xis:message-for="email"></div>
    <xis:global-messages/>
</form>
```

Element syntax for field messages:

```html
<xis:message message-for="email"/>
<xis:global-messages/>
```

Validation messages are resolved from message property files on the classpath. Application files override the XIS
defaults.

```properties
# messages.properties
validation.email=Please enter a valid email address
validation.mandatory=${label} is required
validation.sku=SKU must look like ABC-1234

user.email=Email
user.password=Password
```

Locale-specific files use the Java language suffix, for example `messages_de.properties`, `messages_fr.properties`,
`messages_es.properties`, or `messages_pl.properties`. XIS ships default messages for English, German, French, Spanish,
and Polish.

For a request locale such as Polish, messages are resolved in this order:

1. `messages_pl.properties`
2. `default-messages_pl.properties`
3. `messages.properties`
4. `default-messages.properties`

The first matching key wins. Application files are the normal place for user-facing text. Default files are framework
fallbacks. Message templates can use `${label}` and validator-specific parameters such as `${minLength}`.

`@LabelKey` lets one validation annotation produce context-specific messages. Without it, a reusable validator such as
`@NotNegative` would need different annotations or different message keys for "total price", "VAT", or "current spring
discount". With `@LabelKey`, the validator stays generic and only the label changes.

```java
public record OrderForm(
        @NotNegative
        @LabelKey("order.total")
        BigDecimal total,

        @NotNegative
        @LabelKey("order.vat")
        BigDecimal vat,

        @NotNegative
        @LabelKey("order.springDiscount")
        BigDecimal springDiscount
) {
}
```

```properties
validation.notNegative=${label} must not be negative
validation.notNegative.global=Please check ${label}

order.total=total price
order.vat=VAT
order.springDiscount=current spring discount
```

If `vat` is invalid, `${label}` resolves to `VAT`. If no `@LabelKey` is present, XIS uses the Java field, parameter, or
record component name as the label key.

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
import one.xis.context.Component;

import java.lang.reflect.AnnotatedElement;

@Component
public class SkuValidator implements Validator<String> {

    private final ProductCatalog catalog;

    public SkuValidator(ProductCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public void validate(String value, AnnotatedElement target, UserContext userContext) throws ValidatorException {
        if (value == null || !value.matches("[A-Z]{3}-[0-9]{4}")) {
            throw new ValidatorException();
        }
        if (!catalog.exists(value)) {
            throw new ValidatorException();
        }
    }
}
```

Custom validation is annotation-based: the form field, record component, or parameter receives your annotation, and the
annotation points to the validator through `@Validate`. Validators can be normal XIS components, so constructor-injected
services or repositories are available for checks that need application state, such as a database lookup.

```java
public record ProductForm(
        @Sku
        String sku
) {
}
```
