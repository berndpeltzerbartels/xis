# XIS Theme

[Documentation map](../../README.md)

`xis-theme` is an optional default design for XIS applications. It is meant for developers who know enough HTML to build
forms, navigation, and simple layouts, but who do not want to design a UI from scratch.

The goal is a visually acceptable prototype with almost no CSS work:

- add one dependency
- use normal HTML plus a few theme classes
- customize a small set of CSS variables when needed

If you want a completely custom design system, do not use `xis-theme`; build your own CSS instead.

## Add The Dependency

Add `xis-theme` next to your runtime dependency.

`build.gradle` for XIS Boot:

```groovy
plugins {
    id "java"
    id "one.xis.plugin" version "0.10.0"
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
    id "one.xis.plugin" version "0.10.0"
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

## Logo

The theme adds a logo to the first `<nav>` element with class `nav` when that navigation does not already contain an
element with class `logo`.

The lookup order is:

1. `public/theme-logo.svg` from your application
2. `public/default-theme-logo.svg` from `xis-theme`

Add your own logo here:

```text
src/main/resources/public/theme-logo.svg
```

You can also write the logo manually when the navigation needs custom markup:

```html
<nav class="nav">
    <div class="logo">
        <img src="/theme-logo.svg" alt="Logo">
    </div>
    <ul>
        <li><a xis:page="/dashboard.html">Dashboard</a></li>
    </ul>
</nav>
```

## Navigation

Use a normal `<nav>` element with class `nav`.

```html
<nav class="nav">
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

Nested lists become dropdown menus. On smaller screens they flow into the navigation instead of requiring custom
JavaScript.

## Grid Layout

Use `col2` through `col11` for simple column layouts.

```html
<main class="wrapper">
    <section class="col3">
        <div>First</div>
        <div>Second</div>
        <div>Third</div>
    </section>
</main>
```

Use `span1` through `span9` when an item should span several columns.

```html
<section class="col4">
    <div class="span2">Wide area</div>
    <div>Small area</div>
    <div>Small area</div>
</section>
```

This grid is intentionally simple. It is for quick prototypes, forms, dashboards, and admin pages.

## Forms

Standard inputs, selects, textareas, fieldsets, labels, and buttons are styled automatically.

```html
<form xis:binding="contact">
    <div class="col2">
        <div>
            <label for="firstName">First name</label>
            <input id="firstName" type="text" xis:binding="firstName">
            <div xis:message-for="firstName"></div>
        </div>

        <div>
            <label for="lastName">Last name</label>
            <input id="lastName" type="text" xis:binding="lastName">
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

## Panels And Messages

The theme includes simple panel classes:

```html
<div class="message">Saved successfully.</div>
<div class="warning">Check the entered values.</div>
<div class="tipp">Use the search field to narrow the list.</div>
```

XIS validation global messages render with the `error` class on their generated list and list items, so the theme or
your `theme.css` can style them.

## When To Stop Using XIS Theme

Use `xis-theme` when the default structure is helpful and only colors, spacing, radius, or logo should change.

Do not fight the theme for a completely different product design. In that case, remove `xis-theme` and provide your own
CSS and assets.
