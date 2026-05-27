# Forms and Validation

[Documentation map](../../README.md)

XIS binds form data to Java objects with `@FormData`. Actions receive typed form objects instead of manually parsing
request parameters.

## Basic Form Binding

```java
package example.users;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;

@Page("/users/new.html")
class NewUserPage {

    @FormData("user")
    UserForm user() {
        return new UserForm("", "");
    }

    @Action
    Class<?> saveUser(@FormData("user") UserForm user) {
        userService.save(user);
        return UserListPage.class;
    }

    record UserForm(String firstName, String email) {
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

`@FormData` methods can use the same lifecycle `load` attribute as `@ModelData`. This only affects methods that
initialize a form, not action parameters that receive submitted form data. `@FormData` on a parameter is only valid on
an `@Action` method, because submitted form values exist only while that action is processed. A `@FormData` method must
return a form object; returning `null` is rejected.

```java
@FormData(value = "user", load = ModelDataLoad.INITIAL)
UserForm emptyUser() {
    return new UserForm("", "");
}

@FormData(value = "user", load = ModelDataLoad.AFTER_ACTION)
UserForm userAfterSave() {
    return new UserForm("Saved", "");
}
```

An action may return form data directly when the action is meant to select or replace the currently edited object.
`@Action` acts as a lifecycle filter here: the method is not a form initializer and is not called by the normal form
model request. It runs only when the matching action is triggered, and then its return value becomes the form data for
the next render.

```java
@Action
@FormData("user")
UserForm selectUser(@ActionParameter("userId") long userId) {
    return UserForm.from(userService.findById(userId));
}
```

## Multiple Submit Actions

One form can have multiple actions.

```java
@Action("saveDraft")
void saveDraft(@FormData("document") DocumentForm document,
               @ActionParameter("mode") String mode) {
    documentService.saveDraft(document);
}

@Action("publish")
Class<?> publish(@FormData("document") DocumentForm document,
                 @ActionParameter("mode") String mode) {
    documentService.publish(document);
    return PublishedDocumentsPage.class;
}
```

```html
<form xis:binding="document">
    <input type="text" xis:binding="title"/>
    <textarea xis:binding="content"></textarea>

    <button type="submit" xis:action="saveDraft">
        <xis:parameter name="mode" value="draft"/>
        Save draft
    </button>
    <button type="submit" xis:action="publish">
        <xis:parameter name="mode" value="publish"/>
        Publish
    </button>
</form>
```

Element syntax:

```html
<xis:form binding="document">
    <xis:input type="text" binding="title"/>
    <xis:textarea binding="content"/>

    <xis:submit action="saveDraft">
        <xis:parameter name="mode" value="draft"/>
        Save draft
    </xis:submit>
    <xis:submit action="publish">
        <xis:parameter name="mode" value="publish"/>
        Publish
    </xis:submit>
</xis:form>
```

Form submit actions support the same child `<xis:parameter>` syntax as action links and action buttons. The submitted
form object is read with `@FormData`; submitter-specific values are read with `@ActionParameter`.

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
class ProductForm {
    private List<String> tags;

    List<String> getTags() {
        return tags;
    }

    void setTags(List<String> tags) {
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

## File Uploads

Use a normal file input in the form and mark the matching form field with `@Upload`. XIS switches the submitted request
to `multipart/form-data`, binds the uploaded file to the form object, and runs upload size checks before the action
method is called.

```java
package example.documents;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;
import one.xis.Upload;
import one.xis.UploadedFile;

@Page("/documents/new.html")
class NewDocumentPage {

    @FormData("document")
    DocumentForm document() {
        return new DocumentForm();
    }

    @Action
    void save(@FormData("document") DocumentForm document) {
        documentService.store(document.title, document.attachment);
    }

    static class DocumentForm {
        public String title;

        @Upload(maxSize = 5 * 1024 * 1024)
        public UploadedFile attachment;
    }
}
```

```html
<form xis:binding="document">
    <input type="text" xis:binding="title"/>
    <input type="file" xis:binding="attachment"/>
    <span xis:message-for="attachment"></span>
    <button type="submit" xis:action="save">Save</button>
</form>
```

Element syntax:

```html
<xis:form binding="document">
    <xis:input type="text" binding="title"/>
    <xis:input type="file" binding="attachment"/>
    <xis:message message-for="attachment"/>
    <xis:submit action="save">Save</xis:submit>
</xis:form>
```

`@Upload` supports `UploadedFile`, `List<UploadedFile>`, `byte[]`, and `String`. `String` uploads are decoded as UTF-8.
Use `List<UploadedFile>` when the input allows several selected files.

Upload limits deliberately have two layers:

- `UploadConfiguration.getMaxRequestSize()` is the early HTTP limit for the complete multipart request. It protects the
  runtime before controller validation starts. When this limit is exceeded, the request can be rejected with HTTP 413.
- `UploadConfiguration.getMaxFileSize()` is the default validation limit for one uploaded file.
- `@Upload(maxSize = ...)` overrides the per-file validation limit for one field or controller parameter.

For user-friendly validation messages, configure the request limit high enough for the largest form submission that
should still reach validation. Keep it finite: it is still the transport-level protection against oversized requests.

```java
import one.xis.UploadConfiguration;
import one.xis.context.Component;

@Component
class DocumentUploadConfiguration implements UploadConfiguration {

    @Override
    public long getMaxFileSize() {
        return 2 * 1024 * 1024;
    }

    @Override
    public long getMaxRequestSize() {
        return 20 * 1024 * 1024;
    }
}
```

## Type Conversion

XIS deserializes submitted form values into the Java types used by the `@FormData` object. Empty strings are treated as
missing values; with `@Mandatory`, that becomes a validation error.

Numbers are parsed in two steps. XIS first accepts the canonical Java/JSON representation, then falls back to the
request locale. That means a German user can enter `1.234,56` for `BigDecimal`, `Double`, or `Float`, while `1234.56`
still works as a canonical value. Integer types also accept locale grouping, such as `1.234` in German or `1,234` in
English, but reject fractional values like `1,5`.

```java
record ProductForm(
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

class MoneyFormatter implements Formatter<BigDecimal> {

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

record ProductForm(
        @UseFormatter(MoneyFormatter.class)
        BigDecimal price
) {
}
```

## Validation

XIS validates submitted form data before the action method executes. If validation fails, the action is not called and
validation messages are available to the template.

Form DTOs can be Java classes or records. On records, put validation annotations directly on the record components; XIS
reads those component annotations during deserialization and validation.

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
class RegisterPage {

    @FormData("user")
    UserForm user() {
        return new UserForm("", "");
    }

    @Action
    void register(@FormData("user") UserForm user) {
        userService.register(user);
    }

    record UserForm(
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

Field messages are shown with `xis:message-for`. Global validation messages are shown with
`<xis:global-messages/>`.

Validation annotations can live on a single field or on the form object itself. Field validation is for one value, such
as `email` or `password`. Object validation is for rules that need the complete form, such as "end date must be after
start date" or "discount must not exceed subtotal".

```html
<form xis:binding="user">
    <div xis:message-for="email"></div>
    <xis:global-messages/>
</form>
```

`<xis:global-messages/>` belongs to the form validation result. Use it for validation messages that are not attached to
one field, for example a cross-field rule or a custom validator that reports a form-level problem. It is not the general
application error area.

When global messages exist, XIS renders this structure inside the `<xis:global-messages/>` element:

```html
<ul class="error">
    <li class="error">Message text</li>
</ul>
```

Style the wrapper element, the generated list, or the shared `error` class in your CSS:

```css
.error {
    color: #b00020;
}

xis\:global-messages ul.error {
    margin: 0;
    padding-left: 1.25rem;
}
```

Element syntax for field messages:

```html
<xis:message message-for="email"/>
<xis:global-messages/>
```

The same field message can be written as an attribute or as a XIS element. These two snippets are equivalent:

```html
<div xis:message-for="email"></div>
```

```html
<xis:message message-for="email"/>
```

Fields, labels, and wrappers can also get visual error state directly. Use `xis:error-class` when XIS should add a CSS
class while a validation message exists for the bound field:

```html
<input id="email" xis:binding="email" xis:error-class="error"/>
<label for="email" xis:binding="email" xis:error-class="error">Email</label>
```

If the element is not a form control and should only point at a field, use `xis:error-binding`. This is useful for labels
or layout wrappers:

```html
<label for="email"
       xis:error-binding="email"
       xis:error-style="color: red; font-weight: bold">
    Email
</label>
```

`xis:error-style` applies inline CSS for the error state. Prefer `xis:error-class` for reusable application styling;
`xis:error-style` is mainly useful for small prototypes or generated markup.

General server errors are handled separately by the browser-side message handler. If a page contains an element with
`id="system-messages"`, XIS can render server errors there. Validation messages may also be copied into that general
message area, but form templates should still use `xis:message-for` and `<xis:global-messages/>` when the user needs to
see the errors next to the form.

Validation messages are resolved from message property files on the classpath. Application files override the XIS
defaults. The same resolved bundle is also available in templates as `messages`, so ordinary UI text can use
`${messages.title}` or `${messages['customer.form.title']}`.

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
record OrderForm(
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

Create a custom annotation with `@Validate`. Use a field annotation when the validator only needs one value.

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
@interface Sku {
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
class SkuValidator implements Validator<String> {

    private final ProductCatalog catalog;

    SkuValidator(ProductCatalog catalog) {
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
record ProductForm(
        @Sku
        String sku
) {
}
```

For cross-field rules, put the validation annotation on the form class or record. The validator receives the complete
object.

```java
package example.validation;

import one.xis.validation.Validate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@Validate(
        validatorClass = DiscountValidator.class,
        messageKey = "validation.discount",
        globalMessageKey = "validation.discount.global"
)
@interface ValidDiscount {
}
```

```java
package example.orders;

import example.validation.ValidDiscount;

@ValidDiscount
record DiscountForm(int subtotal, int discount) {
}
```

```java
package example.validation;

import example.orders.DiscountForm;
import one.xis.UserContext;
import one.xis.validation.Validator;
import one.xis.validation.ValidatorException;

import java.lang.reflect.AnnotatedElement;

class DiscountValidator implements Validator<DiscountForm> {

    @Override
    public void validate(DiscountForm form, AnnotatedElement target, UserContext userContext)
            throws ValidatorException {
        if (form.discount() > form.subtotal()) {
            throw new ValidatorException();
        }
    }
}
```

Object validation usually belongs in `<xis:global-messages/>`, because the problem is attached to the submitted form as a
whole rather than to one input field. You can still provide both a field message key and a global message key; the global
message is the one users normally see for cross-field rules.

## Action Validation Failures

Most validation should happen before the action method runs: use `@Mandatory`, built-in validation annotations, or custom
annotations based on `@Validate`. There are cases where the rule belongs to the action itself, for example when a delete
action is only allowed while related data is still empty. Creating a special validation annotation just for that action
would add a lot of ceremony and would hide the rule in the wrong place.

For this special case, throw `ValidationFailedException` from an `@Action` method or from a service called by that
action. XIS catches it during action processing and returns normal validation messages. The same exception thrown while
loading model data is treated as an application error, because model data loading should not be a user-correctable
validation flow.

```java
import one.xis.Action;
import one.xis.ActionParameter;
import one.xis.validation.ValidationFailedException;

class ProjectActions {

    private final ProjectService projects;

    @Action
    void deleteProject(@ActionParameter("projectId") long projectId) {
        if (!projects.pipelineIsEmpty(projectId)) {
            throw new ValidationFailedException("project.delete.pipelineNotEmpty");
        }
        projects.delete(projectId);
    }
}
```

The exception can add global messages, field messages, or both. Message keys are resolved with the same message resolver
used by normal validation.

```java
throw new ValidationFailedException("project.delete.pipelineNotEmpty")
        .addFieldMessage("/project/name", "project.name.notDeletable");
```

Use this exception sparingly. It is a bridge for action-specific business validation, not a replacement for ordinary form
validation annotations.
