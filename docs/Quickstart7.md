# Using Expressions and Variables in XIS Templates

XIS supports `${...}` expressions throughout templates to insert dynamic values or bind data from your Java controller.
These expressions are not limited to text content — you can use them in:

- Plain text inside elements
- Any attribute value (partially or entirely)

### Examples of Expression Usage

```html
<!-- Insert variable value as text -->
<span>Hello, ${user.name}!</span>

<!-- Use inside attributes -->
<a xis:action="viewUser">
    <xis:parameter name="id" value="${user.id}"/>
</a>

<!-- Build dynamic CSS class -->
<div class="user-card ${user.status}"></div>

<!-- Target widget container dynamically -->
<a xis:widget="UserWidget" xis:target-container="${targetId}">Details</a>
```

### Accessing Nested Properties

You can access nested properties using dot notation:

```html
<p>${person.address.street}</p>
```

This fetches the `street` property from `address`, which itself is a property of `person`.

### Use with XIS Tags

Most XIS tags support variable expressions. For example:

```html

<ul>
    <li xis:repeat="item:items">${item.name}</li>
</ul>
```

### Tip

When using expressions inside attributes, they can be the full value or part of it:

```html
<a xis:widget="$(widgetId}" xis:target-container="${targetId}">Details</a>
```

These expression capabilities make it easy to write clean, data-driven HTML without verbose scripting.

