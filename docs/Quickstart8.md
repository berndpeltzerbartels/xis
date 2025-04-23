# Quickstart – XIS Template Tags

In this section, you’ll learn how to use the most important XIS template tags with short, practical examples. These tags
help you display dynamic content, define actions, and bind form data without writing custom JavaScript. All examples
assume HTML with the XIS namespace:

```html

<html xmlns:xis="https://xis.one/xsd">
```

---

## Repeating Content with `xis:foreach` and `xis:repeat`

Both tags render items from a collection, but they behave differently:

- `xis:foreach`: Repeats the **contents** of the tag.
- `xis:repeat`: Repeats the tag **itself**.

### Syntax

- The part before `:` is the **variable name** you can use inside the block.
- The part after `:` is the **collection** (or array) to iterate over.

### Example (static list)

```html
<!-- repeat duplicates the entire <li> tag -->
<ul>
    <li xis:repeat="item:items">${item}</li>
</ul>

<!-- foreach only duplicates the content -->
<xis:foreach var="item" items="${items}">
    <li>${item}</li>
</xis:foreach>

<!-- another foreach example with outer tag -->
<ul xis:foreach="item:items">
    <li>${item}</li>
</ul>
```

### Corresponding Java Controller

```java

@Widget
public class ListControllerExample {

    @ModelData("items")
    public Collection<String> listItems() {
        return List.of("Apple", "Banana");
    }
}
```

### Rendered HTML (with items = ["Apple", "Banana"])

```html

<ul>
    <li>Apple</li>
    <li>Banana</li>
</ul>
```

Both approaches are valid – use `xis:repeat` when you want to repeat the same tag, and `xis:foreach` when you're
wrapping multiple elements.

> Note: In forms with `xis:binding`, both tags automatically shift the binding context to the current element in the
> loop. This enables repeated form fields like `xis:binding="city"` to bind correctly to each item.

> Note: In forms with `xis:binding`, both tags automatically shift the binding context to the current element in the
> loop. This enables repeated form fields like `xis:binding="city"` to bind correctly to each item.

**TODO for documentation:** How to repeat complete forms for a collection using `xis:foreach` outside `<form>`. to the
current list element, allowing you to bind its fields directly.

---

## Action Links with `xis:action` and `<xis:a>`

Trigger backend actions without JavaScript:

```html
<!-- Standard anchor tag -->
<a xis:action="saveData">Save</a>

<!-- XIS tag version -->
<xis:a action="cancelOperation">Cancel</xis:a>
```

Both variants trigger the specified `@Action` method in your controller.

---

## Passing Parameters with `<xis:parameter>`

Use this tag inside a link to pass parameters to an action:

```html
<a xis:action="viewDetails">
    <xis:parameter name="id" value="${item.id}"/>
    View Details
</a>
```

You can add multiple `<xis:parameter>` tags as needed.

---

## Dynamic Navigation with `xis:widget` and `xis:page`

Direct links to widgets or pages without actions:

```html
<!-- Load widget into a container -->
<a xis:widget="UserDetailsWidget" xis:target-container="main">Show Details</a>

<!-- Navigate to page -->
<a xis:page="/profile.html">Profile</a>
```

You may use expressions like `xis:widget="${dynamicWidget}"`.

---

## Widget Containers with `xis:widget-container`

Used as dynamic placeholders for rendering widgets:

```html
<!-- With default widget -->
<div xis:widget-container="main" xis:default-widget="OverviewWidget"></div>

<!-- Empty container -->
<xis:widget-container container-id="sidePanel"/>
```

The container receives output from actions that return `WidgetResponse`. When using `xis:action`, use
`xis:target-container` to specify the destination.

---

## Form Binding with `xis:binding`

XIS binds forms directly to Java objects.

### Basic Form

```html

<form xis:binding="person" xis:action="save">
    <input type="text" xis:binding="name"/>
    <input type="email" xis:binding="email"/>
    <button type="submit">Submit</button>
</form>
```

Controller:

```java

@Page("/person.html")
public class PersonPage {
    @FormData("person")
    public PersonData getPerson() {
        return new PersonData();
    }

    @Action("save")
    public void save(@FormData("person") PersonData data) { ...}
}
```

Validation messages can be shown with `${validation.messages.name}`and  `${validation.messages.email}`. We left out.


---

With these tags, you can build powerful, reactive interfaces without custom JS. XIS handles the data binding, rendering,
and server communication for you.

(Next: Nested widgets and custom tag usage.)

