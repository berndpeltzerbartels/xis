# Custom JavaScript

[Documentation map](../../../README.md)

XIS applications are single-page applications. Do not rely on `<script>` tags inside page or frontlet templates:
browsers do not execute script tags that are inserted into the DOM later as part of a page/frontlet update.

Use a JavaScript extension when project code needs browser-side behavior that is not covered by normal XIS templates.

Integration tests do not run this JavaScript in a real browser. They use GraalVM JavaScript plus XIS' small Java
implementation of common browser objects. This is good for fast tests of XIS DOM behavior, custom EL functions, and
simple DOM manipulation, but it is not a full Web Platform implementation. See
[Integration-test browser model](integration-test-browser.md) for the supported objects and the known limits.

Write extension files as strict, browser-compatible plain JavaScript. XIS bundles extension files into the generated
browser runtime; they are not loaded as JavaScript modules. Do not use `export` / `import` syntax in these files.

XIS exposes only a small public browser API as `window.XIS`. The internal application runtime is intentionally not
published as `window.app`, so separate static scripts cannot mutate framework internals by accident. JavaScript extension
files can use this API while the bundle is loaded, for example to register expression-language functions.

## Register Extension Files

Create this file:

```text
src/main/resources/META-INF/xis/js/extensions
```

List one JavaScript resource path per line:

```text
js/app-functions.js
js/analytics.js
```

Put the referenced files under `src/main/resources`:

```text
src/main/resources/js/app-functions.js
src/main/resources/js/analytics.js
```

XIS reads every `META-INF/xis/js/extensions` file on the classpath, loads the listed JavaScript files, and bundles them
with the XIS browser runtime. This also works from dependency JARs.

## Add Custom EL Functions

Custom expression-language functions are JavaScript functions registered in the browser runtime.

`src/main/resources/js/app-functions.js`

```javascript
XIS.addElFunction('initials', function(value) {
    if (!value) {
        return '';
    }
    return String(value)
        .split(/\s+/)
        .filter(Boolean)
        .map(part => part.substring(0, 1).toUpperCase())
        .join('');
});
```

Register that file:

```text
# src/main/resources/META-INF/xis/js/extensions
js/app-functions.js
```

Use the function in a template:

```html
<span>${initials(user.displayName)}</span>
```

Function arguments can be expressions, just like built-in EL functions:

```html
<span>${initials(trim(user.displayName))}</span>
```

## Global Browser Behavior

JavaScript extensions can also add application-wide browser behavior:

```javascript
document.addEventListener('click', event => {
    const target = event.target.closest('[data-track]');
    if (!target) {
        return;
    }
    console.debug('tracked click', target.dataset.track);
});
```

Keep this kind of code small and defensive. XIS will load it for the application, not for a single page only.

## Submit A XIS Form From JavaScript

Most forms should be submitted by a normal XIS submit button:

```html
<button type="submit" xis:action="save">Save</button>
```

For browser interactions that are not naturally a button click, for example drag and drop, a JavaScript extension can
submit an existing XIS form by its ordinary HTML `id`:

```html
<form id="moveForm" xis:binding="move">
    <input type="hidden" xis:binding="from" id="fromField">
    <input type="hidden" xis:binding="to" id="toField">
</form>
```

```javascript
function submitMove(from, to) {
    document.getElementById('fromField').value = from;
    document.getElementById('toField').value = to;
    XIS.submitForm('moveForm', 'doMove');
}
```

The second argument is the `@Action` name. XIS still performs normal form binding, validation, and action response
handling; the JavaScript only decides when to submit.

## CSS And Static Assets

CSS does not have the same execution problem as JavaScript. Put public files in:

```text
src/main/resources/public/
```

Reference them from templates without the `public` segment:

```html
<link rel="stylesheet" href="/css/app.css">
<img src="/images/logo.png" alt="Logo">
```

CSS and JavaScript files under classpath `public` resources are also added to the root page automatically. Use
`META-INF/xis/js/extensions` instead when JavaScript should become part of the XIS browser runtime bundle. See
[Runtime and dependencies](../runtime-and-dependencies.md#static-resources).

If you use `xis-theme`, prefer `public/theme.css` for small variable overrides. See [XIS theme](theme.md).

## When To Use This

Use custom JavaScript for small browser-side extension points, custom EL functions, third-party browser libraries, or
application-wide instrumentation.

Do not use it to reimplement XIS navigation, form binding, validation, or frontlet updates. Those are framework
features and should stay in Java controllers and XIS templates.
