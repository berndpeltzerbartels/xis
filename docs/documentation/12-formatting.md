## 13. Formatting

### Motivation

In XIS, all formatting and parsing of user input is handled on the **server**. Unlike many web frameworks that require
custom JavaScript or complex client-side configuration, XIS deliberately avoids introducing TypeScript or pattern-based
formatting in HTML attributes.

This decision follows a core principle of XIS: **minimize client logic and configuration**, and handle complexity where
it can be better controlled — on the server. Formatting, especially when it depends on the user's locale, is one of
those areas.

### Server-side Formatters

A formatter in XIS is a server-side component that can both format and parse a value of a certain type, depending on the
locale and time zone. It uses the following interface:

```java

@ImportInstances
public interface Formatter<T> {
    String format(T t, Locale locale, ZoneId zoneId);

    T parse(String s, Locale locale, ZoneId zoneId);
}
```

The same formatter is responsible for converting user input (e.g. "1.234,56" in German) into a Java object — **and** for
rendering the same value back into an input field in the appropriate locale format.

### Built-in Support

For many basic types, deserialization works out of the box:

- `int`, `long`, `double`, `BigDecimal` (using locale-aware parsing)
- `LocalDate` (in ISO or common localized formats)

For others, you may register your own formatter and annotate a field or method parameter accordingly.

### Using a Formatter Directly

You can annotate a DTO field or method parameter directly with a formatter class:

```java

@UseFormatter(GermanDateFormatter.class)
private LocalDate birthDate;
```

### Using Meta-Annotations

Alternatively, you can create your own meta-annotation and associate it with a formatter:

```java

@Retention(RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@UseFormatter(GermanDateFormatter.class)
public @interface GermanDate {
}

// Usage
@GermanDate
private LocalDate birthDate;
```

This approach is similar to the one used in validation annotations and provides a more expressive and reusable syntax.

### Example: Custom LocalDate Formatter

```java

@Component // or @XISComponent (if Spring is not used)
public class GermanDateFormatter implements Formatter<LocalDate> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public String format(LocalDate date, Locale locale, ZoneId zoneId) {
        return date.format(FORMATTER);
    }

    @Override
    public LocalDate parse(String input, Locale locale, ZoneId zoneId) {
        return LocalDate.parse(input, FORMATTER);
    }
}
```

### Server-Side Formatting: Pros and Trade-offs

**Advantages:**

- Locale-sensitive logic remains consistent and testable
- No need to duplicate rules in JavaScript
- Forms stay declarative and minimal

**Trade-off:**

- Custom client-side widgets (e.g. date pickers) must align with accepted server formats.

You may encounter issues where a client-side date picker submits ISO-formatted values, even if the user’s locale is
German. In that case, your formatter must be able to handle multiple possible formats.

### Mini Demo

```html

<form xis:binding="birthdayForm">
    <label for="birth">Birth Date:</label>
    <input type="text" id="birth" xis:binding="birthDate"/>
    <div>${validation.messages.birthDate}</div>
    <button id="save" xis:action="submitBirthday">Speichern</button>
</form>
```

```java
public class BirthdayFormModel {
    @GermanDate
    @Mandatory
    private LocalDate birthDate;
}

@Pagelet
public class BirthdayPage {

    @FormData("birthdayForm")
    public BirthdayFormModel model() {
        return new BirthdayFormModel();
    }

    @Action
    public void submitBirthday(@FormData("birthdayForm") BirthdayFormModel model) {
        // handle data
    }
}
```

### Summary

- XIS uses server-side formatters to parse and format values.
- A `Formatter<T>` must implement both `format(...)` and `parse(...)`.
- Formatters can be used either directly or via custom meta-annotations.
- The default behavior supports common types; custom logic can be added.
- Client-side formatting is intentionally avoided for consistency and maintainability.

---

**TODO:**

- Ensure that both deserialization **and** serialization logic is actually used. Please verify this with an integration
  test.
- Support meta-annotations with `@UseFormatter`, like in the example.
- Implement meta-annotation capability if not yet available.
