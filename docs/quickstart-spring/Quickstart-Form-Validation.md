# Quickstart Guide – Form Validation (Part 2)

XIS supports automatic form validation, including type checks and custom annotations. This guide shows how to:

- Display validation errors near form fields
- Use localized messages from `validation.messages`
- Add custom validation with annotations like `@EMail`

---

## ✅ What you’ll build

A form with `name` and `email`, where:

- `email` must follow a valid format (`@EMail`)
- Both fields are required (`@Mandatory`)

---

## 1. The Model Class

```java
public class PersonData {

    @Mandatory
    private String name;

    @Mandatory
    @EMail
    private String email;

    // Getters and setters omitted for brevity
}
```

---

## 2. The Template

```html

<form xis:action="save" xis:binding="person">
    <div>
        <label>Name:</label>
        <input type="text" xis:binding="name"/>
        <div>${validation.messages.name}</div>
    </div>
    <div>
        <label>Email:</label>
        <input type="text" xis:binding="email"/>
        <div>${validation.messages.email}</div>
    </div>
    <button type="submit">Save</button>
</form>
```

---

## 3. The Controller

```java

@Page("/person.html")
public class PersonController {

    private PersonData data = new PersonData();

    @FormData("person")
    // no @ModelData needed since data is not displayed outside the form
    public PersonData getPersonData() {
        return data;
    }

    @Action("save")
    public void save(@FormData("person") PersonData person) {
        this.data = person;
    }
}
```

---

## 4. Messages

No messages need to be defined manually — basic messages like `validation.mandatory` and `validation.email` are already
included in the default message files delivered with XIS (currently for German and English). This includes validation
for `@EMail`, which does not require any custom configuration.

You can override or add your own by providing `validation.messages_de.properties` or similar files in your project’s
resources.

```properties
validation.email=Ungültige E-Mail-Adresse
validation.email.global=Ungültige E-Mail-Adresse
```

---

## 5. Global Messages

To display general (non-field-specific) validation messages, use:

```html

<ul>
    <li xis:repeat="message:validation.globalMessages">${message}</li>
</ul>
```

We’ll explore the use of `xis:repeat` in more detail in a later chapter.

---

## ✅ Summary

- Use `@Mandatory`, `@EMail`, etc. for field validation
- Messages come from `validation.messages*.properties`
- Use `${validation.messages.field}` in templates to display errors
- You can define your own annotations using `@Validate`

Next up: lists, nested objects, and validation of collections

