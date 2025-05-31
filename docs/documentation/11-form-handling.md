## 11. Form Handling

Form handling in XIS is designed to be simple and annotation-driven. You can define a form model and link it to input
fields in the HTML template using `xis:binding`.

### Defining the Form Model

```java
public class ProductForm {
    @Mandatory
    private String name;

    private int price;

    // getters and setters
}
```

### Binding the Model

```java

@Page
public class ProductPage {

    @ModelData
    public ProductForm form() {
        return new ProductForm();
    }

    @FormData
    public void submit(ProductForm form) {
        // process form
    }
}
```

### Template Example

```html

<form xis:action="submit">
    <input type="text" xis:binding="name"/>
    <input type="number" xis:binding="price"/>
    <button>Submit</button>
</form>
```

### Field-Level Validation Messages

Validation messages are automatically populated into the model using the `validation.messages` map. Global messages are
stored in `validation.globalMessages`.

```html

<div>
    <input type="text" xis:binding="property2" id="field2"/>
    <label for="field2">Property 2</label>
    <div>${validation.messages.property2}</div>
</div>

<ul>
    <li xis:repeat="message:validation.globalMessages">${message}</li>
</ul>
```

Alternatively, you can use `xis:foreach`:

```html

<xis:foreach item="message" in="validation.globalMessages">
    <li>${message}</li>
</xis:foreach>
```

### Styling Error Labels

You can use the `xis:error-class` attribute together with `xis:binding` to automatically apply a CSS class (e.g. "
error")
when the field has a validation issue:

```html
<label xis:binding="price" xis:error-class="error">Price</label>
```

This approach avoids hardcoding the class and keeps the syntax declarative.

### Automatic Validation of Incompatible Values

If a field expects a type like `int`, and the user submits an incompatible value like `"bla"`, XIS will automatically
treat it as a validation error. This behavior is built-in and ensures that form data is only accepted if it matches the
expected types.

Validation messages for these errors can be provided in locale-specific files such as:

```properties
# validation.messages_de.properties
validation.int=Bitte geben Sie eine gültige Zahl ein.
validation.int.global=Ungültiger Zahlenwert.
# validation.messages.properties (English)
validation.int=Please enter a valid number.
validation.int.global=Invalid numeric value.
```

For English and German, these messages are already included in the XIS core, so you don't need to provide them — but you
can override them to customize the wording.

[Kapitel 10: Pagelets (`@Pagelet`) ←](10-pagelets.md) | [Kapitel 12: Custom Validation →](12-custom-validation.md)