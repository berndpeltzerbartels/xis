# XIS JavaScript Runtime

This module contains the browser-side runtime of XIS.

Most end users do not depend on it directly. It is brought in through the selected XIS runtime such as `xis-boot` or
`xis-spring`.

## What It Does

The JavaScript runtime is responsible for:

- SPA-style navigation without full page reloads
- invoking XIS actions and loading model data
- updating pages and widgets in the browser
- reacting to server-triggered refresh events

## Refresh Events

XIS uses refresh events as lightweight notifications from server to browser.

Important design rule:

- refresh events only carry event keys
- they do not carry application data payloads

When the browser receives such an event, it reloads the affected page or widget data through the regular XIS HTTP
flow.

This means the push channel stays simple and does not become a second API layer.

## Transport

The preferred transport for refresh notifications is Server-Sent Events (SSE).

The browser subscribes to:

- `/xis/events`

The client identifier is sent as a query parameter because native browser `EventSource` connections do not allow custom
request headers.

## Adding Custom JavaScript

Custom JavaScript is bundled into the generated XIS browser runtime.

The main supported mechanism is classpath-based registration through:

- `META-INF/xis/js/extensions`

This file contains one classpath resource path per line. Each listed JavaScript file is loaded and appended to the
generated browser bundle.

Example:

`META-INF/xis/js/extensions`

```text
js/my-extension.js
js/my-el-functions.js
```

If those files are available on the classpath, they are bundled into `/bundle.min.js`.

### Recommended Variant

At the moment, the classpath-list mechanism is the reliable and recommended way to add custom JavaScript.

The relevant loader is:

- `one.xis.js.JavascriptExtensionLoader`

### Annotation-Based Variant

There is also an annotation:

- `@JavascriptExtension("path/to/file.js")`

This indicates that an annotation-based registration path was planned.

However, based on the current code, this path is less clearly wired into the final generated JavaScript bundle than the
`META-INF/xis/js/extensions` mechanism. For that reason it should currently be treated as secondary or experimental
unless verified in the specific application setup.

If you want a predictable result today, prefer `META-INF/xis/js/extensions`.

## Extending The EL

There is a special case for extending the XIS expression language (EL).

The browser runtime provides a global EL function registry based on `ELFunctions`. Custom functions can be added with:

```javascript
elFunctions.addFunction("myFunction", function(value) {
    return value;
});
```

This is useful when you want to call custom functions from XIS expressions in templates.

Example:

```javascript
elFunctions.addFunction("greet", function(name) {
    return "Hello " + name;
});
```

Then in HTML:

```html
<span>${greet(user.name)}</span>
```

Relevant implementation files:

- `src/main/js/classes/parse/ELFunctions.js`
- `src/main/js/classes/parse/AstGenerator.js`

## Notes About Scope

- Custom JavaScript added this way becomes part of the global browser bundle.
- It is loaded eagerly, not on demand.
- Refresh events should stay notification-only. Do not use custom JavaScript to turn the refresh channel into a second
  business API unless that is a deliberate architectural decision.

## Notes For Contributors

- Keep transport logic lightweight. Refresh events are notifications, not data messages.
- If you change application startup or refresh behavior, review both:
  - `src/main/js/app`
  - `src/main/js/test`
- Integration-test helper names should describe behavior, not protocol details.
