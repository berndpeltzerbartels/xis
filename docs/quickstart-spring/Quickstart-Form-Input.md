# Quickstart Guide – Forms and Input (Part 1)

XIS supports HTML forms with automatic data binding and validation. In this first example, you’ll learn how to:

- Define a form-bound model class
- Create a form that posts data to a controller
- Bind the form to a `@FormData` method
- Display the submitted values using the same object

This example does **not** include custom validation – but you will still see automatic error messages for type
mismatches (e.g. entering text instead of a number).

---

## ✅ What you’ll build

A form to submit **name** and **birth year**, mapped to a `PersonalData` object.

---

## 1. The Model Class

```java
public class PersonalData {
    private String name;
    private int birthYear;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }
}
```

---

## 2. The Page Template

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd">
<head>
    <title>Form Example</title>
</head>
<body>
<form xis:action="submit" xis:binding="personalData">
    <div>
        Name: <input type="text" name="name" xis:binding="name" id="name"/><br/>
        <div>${validation.messages.name}</div>
    </div>
    <div>
        Birth Year: <input type="text" name="birthYear" xis:binding="birthYear" id="birthYear"/><br/>
        <div>${validation.messages.birthYear}</div>
    </div>
    <button type="submit">Submit</button>
</form>

<p>Hello ${personalData.name}, born in ${personalData.birthYear}.</p>
</body>
</html>

<p>Hello ${personalData.name}, born in ${personalData.birthYear}.</p>
</body>
</html>
```

Note: The fields match the properties of `PersonalData`.

---

## 3. The Controller

```java

@Page("/form/FormExample.html")
public class FormExamplePage {

    private PersonalData data = new PersonalData();

    @FormData("personalData")
    @ModelData("personalData") // only if you want to display it outside form, too
    public PersonalData getFormData() {
        return data;
    }

    @Action("submit")
    public void submit(@FormData("personalData") PersonalData form) {
        this.data = form;
    }
}
```

---

## ✅ Summary

- Use a dedicated model class to represent form data
- Use `@FormData` to bind and display data
- Automatic validation is built in — try submitting a non-number as birth year!
- No `@FormParameter` needed – values are mapped by name to fields in the object

Next up: validation feedback, optional/required fields, and complex object structures

