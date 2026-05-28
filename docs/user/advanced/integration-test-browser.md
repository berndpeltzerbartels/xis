# Integration-Test Browser Model

[Documentation map](../../../README.md)

XIS integration tests run the XIS JavaScript runtime in GraalVM JavaScript, but they do not start a real browser.
The test module provides a small Java implementation of common browser objects so tests can render templates, click
elements, submit forms, navigate, and inspect the resulting DOM quickly.

Use this environment for XIS behavior. Use E2E tests with a real browser when browser behavior itself is important.

Because tests run under GraalVM, some JavaScript or host-object interactions may be less forgiving than a browser would
be. Keep custom JavaScript strict and explicit: declare variables, avoid relying on accidental globals, avoid assigning
to read-only properties, and prefer simple browser APIs that are listed below.

Extension files are bundled as plain scripts, not as JavaScript modules. `export` and `import` are not supported in XIS
custom JavaScript extension files.

## Available Global Objects

The integration-test script exposes these browser-like globals:

| Global | What It Provides |
| --- | --- |
| `document` | Parsed DOM tree for the current XIS page. |
| `window` | Browser window facade with location, history, scroll state, and references to document/storage/console. |
| `localStorage` | In-memory `Storage` implementation for the test client. |
| `sessionStorage` | In-memory `Storage` implementation for the test client. |
| `console` | `log`, `debug`, `warn`, and `error`. `console.error` fails the test. |
| `Node` | DOM node constants such as `Node.ELEMENT_NODE`. |
| `setTimeout` | Runs the callback after a delay in a Java thread. |
| `atob` | Base64 decoder. |
| `encodeURIComponent` | URL component encoder. |

XIS also binds internal helpers used by its own runtime, such as `htmlToElement` and `backendBridge`.

## Document

`document` supports the operations most XIS integration tests need:

```javascript
document.querySelector('#save');
document.querySelectorAll('.item');
document.getElementById('name');
document.getElementsByTagName('input');
document.createElement('div');
document.createTextNode('Hello');
document.createComment('note');
document.createDocumentFragment();
```

The common properties below are also available:

```javascript
document.documentElement;
document.body;
document.head;
document.title;
document.cookie;
document.location;
document.defaultView;
```

`document.title` and `document.cookie` can be read and written in JavaScript.

## Element And Node

Elements support the DOM operations used by the XIS runtime and by typical integration tests:

```javascript
element.getAttribute('data-id');
element.setAttribute('data-id', '42');
element.hasAttribute('disabled');
element.removeAttribute('disabled');
element.appendChild(child);
element.insertBefore(newNode, markerNode);
element.removeChild(child);
element.remove();
element.cloneNode();
element.click();
element.dispatchEvent(event);
element.querySelector('input[name="email"]');
element.querySelectorAll('.error');
```

Common DOM properties are exposed through Java getters and setters, so JavaScript can use property syntax:

```javascript
element.innerText = 'Saved';
element.innerHTML = '<span>Saved</span>';
element.textContent = 'Saved';
element.className = 'active selected';

element.innerText;
element.innerHTML;
element.textContent;
element.localName;
element.tagName;
element.parentNode;
element.firstChild;
element.nextSibling;
element.childNodes;
element.classList;
```

Form element implementations exist for `input`, `textarea`, `select`, and `option`.

Drag and drop tests can use `one.xis.test.dom.DragAndDrop`. It dispatches `dragstart`, `dragover`, and `drop` events
with one shared `DataTransfer` object. The test browser model implements the `setData` and `getData` methods used by
XIS drag and drop actions.

## Window

`window` contains:

```javascript
window.document;
window.location;
window.history;
window.localStorage;
window.sessionStorage;
window.console;
window.scrollTo(0, 0);
window.scrollX;
window.scrollY;
```

`window.addEventListener(...)` exists as a no-op. It is present so browser-oriented code can be loaded, but it does not
simulate the browser event loop.

## Location And History

`location` supports the fields XIS navigation needs:

```javascript
location.pathname;
location.href;
location.origin;
location.search;
```

`history` supports:

```javascript
history.pushState(state, title, url);
history.replaceState(state, title, url);
history.back();
history.forward();
```

The Java test API also exposes inspection helpers such as the current state and history entries.

## Storage

`localStorage` and `sessionStorage` support the normal methods:

```javascript
localStorage.setItem('key', 'value');
localStorage.getItem('key');
localStorage.removeItem('key');
```

Values are kept in memory for the test client. They are not browser storage and they do not persist outside the test
context.

## What Is Not A Real Browser

The integration-test browser model intentionally does not implement the complete Web Platform. In particular, do not
rely on it for:

- CSS cascade, layout, flex/grid position, dimensions, computed styles, fonts, or media queries
- canvas, SVG rendering, images, video, audio, WebGL, or animation frames
- focus management, pointer/touch/keyboard event fidelity, bubbling/capturing semantics, or default browser actions
- real networking APIs such as `fetch`, `XMLHttpRequest`, `WebSocket`, or `EventSource`
- real cookie handling, cookie attributes, SameSite/Secure behavior, or browser privacy rules
- Web Components, Shadow DOM, mutation observers, intersection observers, resize observers, or performance APIs
- browser security behavior such as CORS, CSP, redirects, popup blocking, or third-party cookie policy
- execution behavior of dynamically inserted `<script>` tags
- JavaScript module loading, including `export` and `import` syntax in extension files

If custom JavaScript depends on one of these areas, cover it with a Playwright E2E test instead of relying only on
`xis-test`.

## Practical Rule

Use integration tests for custom JavaScript when it manipulates the XIS-rendered DOM in simple ways, registers EL
functions, reads or writes attributes, or reacts to XIS-triggered clicks.

Use E2E tests when the JavaScript depends on actual browser APIs, browser layout, network behavior, or realistic events.
