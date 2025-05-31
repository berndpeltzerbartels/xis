## 12. Custom Validation

XIS supports annotation-based validation. Unlike some frameworks that mix declarative and programmatic validation, XIS
encourages a fully declarative approach using annotations only. This makes validation rules clearer, more reusable, and
easier to manage.

### Built-in Validation

XIS includes basic validations like `@Mandatory`, which replaces the common `@NotNull` constraint. More complex
validations like email format (`@EMail`) are also supported.

### Custom Validators

To define custom validation logic, you can implement your own `Validator` and link it to a custom annotation. Here is a
complete step-by-step example for validating an IBAN string.

---

### Step 1: Implement the Validator

```java

@Component // in case we are using Spring
class IBANValidator implements Validator<String> {

    private static final Pattern IBAN_PATTERN = Pattern.compile("^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$");

    @Override
    public void validate(String value, AnnotatedElement annotatedElement) throws ValidatorException {
        if (value == null || value.isBlank()) {
            return; // Optional values are allowed unless annotated with @Mandatory
        }

        // Basic pattern check
        if (!IBAN_PATTERN.matcher(value).matches()) {
            throw new ValidatorException();
        }

        // Checksum validation
        String rearranged = value.substring(4) + value.substring(0, 4);
        StringBuilder numericIban = new StringBuilder();
        for (char ch : rearranged.toCharArray()) {
            if (Character.isLetter(ch)) {
                numericIban.append((int) ch - 55); // A = 10, B = 11, ..., Z = 35
            } else {
                numericIban.append(ch);
            }
        }

        try {
            String numeric = numericIban.toString();
            int mod = 0;
            for (int i = 0; i < numeric.length(); i++) {
                int digit = Character.digit(numeric.charAt(i), 10);
                mod = (mod * 10 + digit) % 97;
            }
            if (mod != 1) {
                throw new ValidatorException();
            }
        } catch (NumberFormatException e) {
            throw new ValidatorException();
        }
    }
}
```

---

### Step 2: Create the Custom Annotation

```java

@Retention(RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Validate(validatorClass = IBANValidator.class, messageKey = "validation.iban", globalMessageKey = "validation.iban.global")
public @interface IBAN {
}
```

---

### Step 3: Use the Annotation in a Form Model

```java
public class BankFormModel {
    @Mandatory
    @IBAN
    private String iban;
    // other fields
}
```

---

### Step 4: Add Validation Messages

Create a file named `validation.messages` or `validation.messages_de.properties` depending on your locale.

```properties
validation.iban=Invalid IBAN
validation.iban.global=The IBAN entered is invalid.
```

Global messages can be used to display a general error for the entire form.

---

### Notes

* Validators must implement the `Validator<T>` interface.
* They are automatically discovered when annotated with `@Component` (if you use Spring).
* XIS instantiates the validators itself.
* All field-based validations are triggered when the form is submitted and bound using `@FormData`.

This approach allows concise, reusable, and testable validation logic integrated seamlessly into XIS forms.

[Kapitel 11: Form Handling ←](11-form-handling.md) | [Kapitel 13: Formatting →](13-formatting.md)