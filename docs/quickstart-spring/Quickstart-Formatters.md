# Quickstart – Formatters and Data Conversion

In XIS, formatters play a key role in both displaying data to the user and parsing user input back into Java objects.
They are used implicitly with `@FormData` in both directions: when data is sent to the client and when it is received
from a form.

---

## Built-in Formatting Support

XIS supports automatic formatting for many basic types, including:

- `String`
- `int`, `Integer`, and other numbers
- `LocalDate`, `LocalDateTime` (with default or localized formats)

You can customize how values are formatted or parsed using the `@Format` annotation on a field.

### Example

```java

@Data
class PersonForm {
    @Format(LocalDateFormatter.class)
    private LocalDate birthday;
}
```

With this, XIS will format the `birthday` field when rendering it to a form, and use the custom parser to read it back
from a form input.

---

## Custom Formatters

To create a custom formatter, implement the `Formatter<T>` interface:

```java
public class LocalDateFormatter implements Formatter<LocalDate> {
    @Override
    public String format(LocalDate localDate, Locale locale, ZoneId zoneId) {
        return String.format("%02d.%02d.%d", localDate.getDayOfMonth(), localDate.getMonthValue(), localDate.getYear());
    }

    @Override
    public LocalDate parse(String s, Locale locale, ZoneId zoneId) {
        var split = s.split("\\.");
        if (split.length == 3) {
            return LocalDate.of(
                    Integer.parseInt(split[2]),
                    Integer.parseInt(split[1]),
                    Integer.parseInt(split[0]));
        }
        throw new IllegalArgumentException("Invalid date format");
    }
}
```

Apply this formatter via `@Format` on fields in form-bound objects.

---

## Formatting in Forms

When a field has a formatter:

- Its value is converted into a string before being inserted into the form.
- The user input is parsed using the formatter when the form is submitted.

This works transparently for both simple and nested objects, and even for collections.

### Example Controller

```java

@Page("/formatting.html")
public class FormattingExamplePage {
    private final PersonForm data = new PersonForm();

    @FormData("person")
    public PersonForm getFormData() {
        return data;
    }

    @Action("save")
    public void save(@FormData("person") PersonForm form) {
        this.data.setBirthday(form.getBirthday());
    }
}
```

### Example Template

```html

<form xis:binding="person" xis:action="save">
    <label for="birthday">Birthday (DD.MM.YYYY):</label>
    <input id="birthday" type="text" xis:binding="birthday"/>
    <div>${validation.messages.birthday}</div>
    <button type="submit">Save</button>
</form>
```

---

## Error Handling

If parsing fails (e.g. user enters `abc` instead of a date), the system:

- Records a validation error (e.g. `InvalidValueError`)
- Retains the submitted (invalid) value where possible
- Makes the error accessible via `${validation.messages.fieldName}`
- Provides a default global message (`validation.invalid.global`) such as `Ungültig` if locale is German

This means your form can show feedback without needing additional validation annotations.

### Global Error Example

```html

<ul>
    <li xis:repeat="msg:validation.globalMessages">${msg}</li>
</ul>
```

---

## Summary

- Formatters apply in both directions: from model to form (display), and form to model (parsing)
- You can annotate fields with `@Format(...)` to control parsing/formatting
- Custom formatters can be used for complex types or locale-specific rules
- Errors are automatically captured and integrated with the validation system
- Parsing failures automatically generate field and global validation messages

