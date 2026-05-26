# XIS Theme

[Documentation map](../../README.md) | [Advanced topics](README.md)

`xis-theme` is the optional default design layer for XIS applications. It is meant for developers who want a presentable
application quickly without designing navigation, forms, grid layout, labels, and validation messages by hand.

The preferred path is:

- add one dependency
- write `theme:*` tags for standard pages
- customize colors, radius, spacing, or logo only when needed

Write normal HTML only when the generated standard structure is not enough.

## Add The Dependency

Add `xis-theme` next to your runtime dependency.

`build.gradle` for XIS Boot:

```groovy
plugins {
    id "java"
    id "one.xis.plugin" version "0.14.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "one.xis:xis-boot" // or xis-spring
    implementation "one.xis:xis-theme"
}
```

`build.gradle` for Spring:

```groovy
plugins {
    id "java"
    id "org.springframework.boot" version "3.3.0"
    id "io.spring.dependency-management" version "1.1.5"
    id "one.xis.plugin" version "0.14.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "org.springframework.boot:spring-boot-starter-web"
    implementation "one.xis:xis-spring"
    implementation "one.xis:xis-theme"
}
```

You do not have to add `<link rel="stylesheet">` tags. XIS automatically adds CSS files from classpath `public`
resources to the root page. XIS itself provides `public/xis-runtime.css` with required runtime styles such as modal
layout. The optional theme provides:

- `public/default-theme.css`: simple variables for colors, spacing, font sizes, and control sizes
- `public/xis.css`: layout, navigation, forms, tables, messages, and component styling

`default-theme.css` is loaded before `xis.css`. `xis-runtime.css` is loaded between them and is also present when
`xis-theme` is not used. See [Runtime and dependencies](../runtime-and-dependencies.md#static-resources) for the full
automatic resource order.

## Navigation

Use `theme:navigation` for the standard theme navigation. It creates the logo, `<nav>`, and list structure used by
`xis.css`. Navigation supports two visible levels: top-level items and dropdown groups.

Original:

```html
<theme:navigation logo="/theme-logo.svg" logo-alt="Acme CRM">
    <theme:nav-item page="/dashboard.html" label="Dashboard"/>
    <theme:nav-group label="Customers">
        <theme:nav-item page="/customers.html" label="All customers"/>
        <theme:nav-item page="/pipeline.html" label="Pipeline"/>
    </theme:nav-group>
    <theme:nav-item modal="HelpModal" label="Help"/>
</theme:navigation>
```

Generated:

```html
<nav class="nav">
    <div class="logo">
        <img src="/theme-logo.svg" alt="Acme CRM">
    </div>
    <ul>
        <li><a xis:page="/dashboard.html">Dashboard</a></li>
        <li>
            <a href="#">Customers</a>
            <ul>
                <li><a xis:page="/customers.html">All customers</a></li>
                <li><a xis:page="/pipeline.html">Pipeline</a></li>
            </ul>
        </li>
        <li><a xis:modal="HelpModal">Help</a></li>
    </ul>
</nav>
```

`theme:nav-item` supports `page`, `frontlet`, `modal`, and `href`. Use `theme:nav-group` for dropdowns.

## Standard Form Page

For a standard form page, write `theme:form-page`. It creates the wrapper, heading, form, fields, messages, and submit
button.

`theme:input`, `theme:textarea`, `theme:checkbox`, `theme:radio`, and `theme:select` require `title`. `binding` stays a
technical field name; `title` is the text shown to the user.

Original:

```html
<theme:form-page title="Edit customer"
                 binding="customer"
                 action="saveCustomer"
                 submit-label="Save">
    <theme:input binding="firstName" title="First name"/>
    <theme:input binding="lastName" title="Last name"/>
    <theme:select binding="stage" title="Stage" options="stages"/>
    <theme:radio binding="preferredContact" title="Preferred contact" options="contactTypes"/>
    <theme:checkbox binding="newsletter" title="Newsletter"/>
    <theme:textarea binding="notes" title="Notes"/>
</theme:form-page>
```

Generated:

```html
<main class="wrapper">
    <h1>Edit customer</h1>
    <form xis:binding="customer">
        <div class="form-field">
            <label for="firstName" xis:binding="firstName" xis:error-class="error">First name</label>
            <input id="firstName" type="text" xis:binding="firstName" xis:error-class="error">
            <div xis:message-for="firstName"></div>
        </div>

        <div class="form-field">
            <label for="lastName" xis:binding="lastName" xis:error-class="error">Last name</label>
            <input id="lastName" type="text" xis:binding="lastName" xis:error-class="error">
            <div xis:message-for="lastName"></div>
        </div>

        <div class="form-field">
            <label for="stage" xis:binding="stage" xis:error-class="error">Stage</label>
            <select id="stage" xis:binding="stage" xis:error-class="error">
                <option xis:repeat="option:stages" value="${option}">${option}</option>
            </select>
            <div xis:message-for="stage"></div>
        </div>

        <button type="submit" xis:action="saveCustomer">Save</button>
    </form>
</main>
```

The controller stays ordinary XIS code:

```java
@Page("/customers.html")
class CustomersPage {

    @FormData("customer")
    CustomerForm customer() {
        return new CustomerForm();
    }

    @ModelData("stages")
    List<CustomerStage> stages() {
        return List.of(CustomerStage.values());
    }

    @ModelData("contactTypes")
    List<ContactType> contactTypes() {
        return List.of(ContactType.values());
    }

    @Action
    void saveCustomer(@FormData("customer") CustomerForm customer) {
        // save
    }
}
```

## Form Fragments

`theme:form` is the smaller variant of `theme:form-page`. Use it when the page already has its own surrounding
structure and only the form itself should be generated. The field syntax is the same: `theme:input`, `theme:textarea`,
`theme:checkbox`, `theme:radio`, `theme:select`, field messages, and the submit action are handled exactly like in a
standard form page.

Original:

```html
<theme:form binding="customer" action="saveCustomer" submit-label="Save">
    <theme:input binding="firstName" title="First name"/>
    <theme:input binding="lastName" title="Last name"/>
</theme:form>
```

Generated:

```html
<form xis:binding="customer">
    <div class="form-field">
        <label for="firstName" xis:binding="firstName" xis:error-class="error">First name</label>
        <input id="firstName" type="text" xis:binding="firstName" xis:error-class="error">
        <div xis:message-for="firstName"></div>
    </div>

    <div class="form-field">
        <label for="lastName" xis:binding="lastName" xis:error-class="error">Last name</label>
        <input id="lastName" type="text" xis:binding="lastName" xis:error-class="error">
        <div xis:message-for="lastName"></div>
    </div>

    <button type="submit" xis:action="saveCustomer">Save</button>
</form>
```

## Input

Use `theme:input` for a complete field with label, input, error class hook, and message.

Original:

```html
<theme:input binding="email" title="E-mail address" type="email"/>
```

Generated:

```html
<div class="form-field">
    <label for="email" xis:binding="email" xis:error-class="error">E-mail address</label>
    <input id="email" type="email" xis:binding="email" xis:error-class="error">
    <div xis:message-for="email"></div>
</div>
```

Normal input attributes are kept:

```html
<theme:input binding="amount" title="Amount" type="number" min="0" step="0.01" placeholder="0.00"/>
```

Use `span` when the generated field wrapper should span several grid columns. The attribute is applied to the generated
`div.form-field`, not to the input element.

Original:

```html
<theme:input binding="notes" title="Notes" span="2"/>
```

Generated:

```html
<div class="form-field span2">
    <label for="notes" xis:binding="notes" xis:error-class="error">Notes</label>
    <input id="notes" type="text" xis:binding="notes" xis:error-class="error">
    <div xis:message-for="notes"></div>
</div>
```

## Select

Use `theme:select` for a complete select field. `options` names the `@ModelData` value used to create `<option>`
elements.

Original:

```html
<theme:select binding="stage" title="Stage" options="stages"/>
```

Generated:

```html
<div class="form-field">
    <label for="stage" xis:binding="stage" xis:error-class="error">Stage</label>
    <select id="stage" xis:binding="stage" xis:error-class="error">
        <option xis:repeat="option:stages" value="${option}">${option}</option>
    </select>
    <div xis:message-for="stage"></div>
</div>
```

`theme:select` also supports `span` with the same wrapper behavior as `theme:input`.

For option objects, use `option-value` and `option-label`.

Original:

```html
<theme:select binding="stage" title="Stage" options="stages" option-value="code" option-label="label"/>
```

Generated option:

```html
<option xis:repeat="option:stages" value="${option.code}">${option.label}</option>
```

Use `option-var` when another variable name reads better:

```html
<theme:select binding="stage"
              title="Stage"
              options="stages"
              option-var="stage"
              option-value="code"
              option-label="label"/>
```

This becomes:

```html
<option xis:repeat="stage:stages" value="${stage.code}">${stage.label}</option>
```

## Textarea, Checkbox, And Radio

Use `theme:textarea` for longer text values. It creates the label, textarea, error class hook, and message in the same
way as `theme:input`.

Original:

```html
<theme:textarea binding="notes" title="Notes" rows="5" span="2"/>
```

Generated:

```html
<div class="form-field span2">
    <label for="notes" xis:binding="notes" xis:error-class="error">Notes</label>
    <textarea id="notes" rows="5" xis:binding="notes" xis:error-class="error"></textarea>
    <div xis:message-for="notes"></div>
</div>
```

Use `theme:checkbox` for boolean values:

```html
<theme:checkbox binding="newsletter" title="Newsletter"/>
```

Radio groups are intentionally closer to `theme:select` than to raw `<input type="radio">`. The options come from
`@ModelData`, and labels are generated by the theme.

Original:

```html
<theme:radio binding="preferredContact"
             title="Preferred contact"
             options="contactTypes"
             option-value="code"
             option-label="label"/>
```

Generated choice:

```html
<label xis:repeat="option:contactTypes">
    <input type="radio" xis:binding="preferredContact" value="${option.code}">
    <span>${option.label}</span>
</label>
```

`theme:radio` also supports `option-var`, `option-value`, and `option-label` with the same meaning as `theme:select`.

## Grid

Use `theme:grid` when you only want to say how many columns the theme should use.
`columns="3"` generates the CSS class `col3`. In the default theme, `col3` means a CSS grid with three equal columns.
Likewise, `columns="2"` generates `col2`, `columns="4"` generates `col4`, and so on up to `col11`.

Original:

```html
<theme:grid columns="3">
    <theme:input binding="firstName" title="First name"/>
    <theme:input binding="lastName" title="Last name"/>
    <theme:select binding="stage" title="Stage" options="stages" span="3"/>
</theme:grid>
```

Generated:

```html
<section class="col3">
    <div class="form-field">
        ...
    </div>
    <div class="form-field">
        ...
    </div>
    <div class="form-field span3">
        ...
    </div>
</section>
```

`span="3"` on a field generates `span3` on the field wrapper. In a `col3` grid, that field uses the full row.

`columns` accepts `2` through `11`. The generated element is a `<section>` by default. Use `as` for another element:

```html
<theme:grid as="div" columns="2">
    ...
</theme:grid>
```

## Validation

The validator treats `theme:form-page`, `theme:form`, `theme:input`, `theme:textarea`, `theme:checkbox`,
`theme:radio`, and `theme:select` as theme syntax, not as generated markup. The extension is loaded from `xis-theme`,
so validation errors point to the `theme:*` line written by the user.

Form field bindings are checked against the `@FormData` object. Theme field tags must have `title`, and the `options`
value of `theme:select` and `theme:radio` is checked as model data usage.

Theme tags and normal XIS markup can be mixed when that makes a page clearer:

```html
<form xis:binding="customer">
    <input xis:binding="id" type="hidden">
    <theme:input binding="firstName" title="First name"/>
    <theme:input binding="lastName" title="Last name"/>
</form>
```

## Customize The Theme

Create `src/main/resources/public/theme.css` in your application when you want to change the basic appearance.

```css
:root {
  --accent: #2563eb;
  --text: #111827;
  --bg: #ffffff;
  --bg-secondary: #f3f4f6;
  --radius: 6px;
}
```

`theme.css` is loaded after the theme CSS, so it can override the variables. Keep this file small. It is intended for
customizing the default theme, not for replacing it.

Useful variables:

| Variable | Purpose |
| --- | --- |
| `--accent` | Primary color for buttons, active navigation, focus borders, and highlights |
| `--text` | Main text color |
| `--muted` | Secondary text color |
| `--bg` | Main background color |
| `--bg-secondary` | Secondary background color for tables and panels |
| `--border` | General border color |
| `--field-border` | Input, select, textarea, and fieldset border color |
| `--radius` | Border radius for inputs, buttons, and panels |
| `--base-font` | Base page font size |
| `--form-font` | Form/control font size |
| `--grid-gap` | Gap between grid columns |

Add your own logo here:

```text
src/main/resources/public/theme-logo.svg
```

Use it from `theme:navigation`:

```html
<theme:navigation logo="/theme-logo.svg" logo-alt="Acme CRM">
    ...
</theme:navigation>
```

If no custom logo is provided, use `/default-theme-logo.svg`.

## Manual HTML And Classes

The generated theme tags cover standard pages. When the page needs a different structure, write normal HTML and use the
same CSS classes directly.

Manual navigation:

```html
<nav class="nav">
    <div class="logo">
        <img src="/theme-logo.svg" alt="Logo">
    </div>
    <ul>
        <li><a xis:page="/dashboard.html">Dashboard</a></li>
        <li>
            <a href="#">Contacts</a>
            <ul>
                <li><a xis:page="/contacts.html">All contacts</a></li>
                <li><a xis:page="/contacts/new.html">New contact</a></li>
            </ul>
        </li>
    </ul>
</nav>
```

Manual grid:

```html
<main class="wrapper">
    <section class="col3">
        <div>First</div>
        <div>Second</div>
        <div>Third</div>
    </section>
</main>
```

Use `span1` through `span9` when an item should span several columns:

```html
<section class="col4">
    <div class="span2">Wide area</div>
    <div>Small area</div>
    <div>Small area</div>
</section>
```

Manual form:

```html
<form xis:binding="contact">
    <div class="col2">
        <div class="form-field">
            <label for="firstName" xis:binding="firstName" xis:error-class="error">First name</label>
            <input id="firstName" type="text" xis:binding="firstName" xis:error-class="error">
            <div xis:message-for="firstName"></div>
        </div>

        <div class="form-field">
            <label for="lastName" xis:binding="lastName" xis:error-class="error">Last name</label>
            <input id="lastName" type="text" xis:binding="lastName" xis:error-class="error">
            <div xis:message-for="lastName"></div>
        </div>

        <div class="span2">
            <button xis:action="save" type="submit">Save</button>
        </div>
    </div>
</form>
```

Use `button-secondary` for a secondary button style:

```html
<button class="button-secondary" xis:action="cancel" type="button">Cancel</button>
```

The theme also includes simple panel and message classes:

```html
<div class="message">Saved successfully.</div>
<div class="warning">Check the entered values.</div>
<div class="tipp">Use the search field to narrow the list.</div>
```

XIS validation global messages render with the `error` class on their generated list and list items, so the theme or
your `theme.css` can style them.
