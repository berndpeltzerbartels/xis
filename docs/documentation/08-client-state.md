# 8. Response Types Of Action-Methods

XIS allows developers to maintain reactive, browser-local state across pagelets and pages using two powerful mechanisms:

---

## 🧠 ClientState (Ephemeral)

ClientState is a volatile memory store per browser tab. It is lost when the tab is closed or refreshed, and does not
persist across sessions.

### Accessing clientState in templates:

```html
<p>Hello, ${clientState.username}!</p>
```

### Updating clientState from Java:

```java
clientState.set("username","Alice");
```

---

## 💾 LocalStorage (Persistent)

LocalStorage provides a persistent key-value store per domain and browser. It survives page reloads and browser
restarts.

### Accessing localStorage in templates:

```html

<div>Theme: ${localStorage.theme}</div>
```

### Updating localStorage from Java:

```java
localStorage.set("theme","dark");
```

> ⚠️ All state variables are automatically reactive — templates that reference them will re-render when values change.
> This reactivity applies page-wide to any visible template, including nested pagelets.

---

## 🔁 Usage Across Components

Both `clientState` and `localStorage` are accessible from **all templates**, regardless of page or pagelet.

You can use these state containers to:

- Control visibility and behavior of UI elements.
- Store user preferences or temporary input.
- Exchange information between pagelets without involving a backend.

---

## 🛠️ Future Feature: LocalDatabase

A third mechanism called `localDatabase` is planned, designed for more structured, indexed, and queryable offline
storage.

---

## 🚫 Deprecated: PageScope

`PageScope` no longer exists. Use `clientState` instead for ephemeral values, or `localStorage` for persistent values.

---

→ [Continue to Chapter 9: Controller Methods](09-controller-methods.md)
