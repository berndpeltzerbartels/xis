## 14. Client State and Reactive Values

XIS supports reactive template updates based on values stored in the **client-side state**, especially through
`localStorage` and `clientState`. This allows elements of the UI to automatically reflect changes without manual
intervention, enhancing user interactivity and responsiveness.

---

### Purpose and Scope

Client-side state mechanisms like `localStorage` and `clientState` are designed for modular frontend logic. They should
**not** be treated as a replacement for global application state — in fact, XIS intentionally discourages centralized
state models to favor **modularity** and **microfrontend compatibility**.

> Recommended use: Isolated UI updates, such as dynamically showing the number of items in a shopping cart or toggling a
> section based on previous user interaction.

> Values stored in `localStorage` or `clientState` can also be used in **other components** or **update other components
**. However, this approach should be used with care and only when necessary.

---

### Using `localStorage` in Templates

Values stored in `localStorage` are directly accessible in EL expressions. Example:

```html
<span>You have ${localStorage.cartSize} items in your cart.</span>
```

* These expressions are automatically re-evaluated when the `cartSize` key in `localStorage` is updated.
* Updates made outside of XIS (e.g. via `window.localStorage.setItem(...)`) may still be picked up, depending on the
  event listeners used. *(TODO: Confirm listener coverage)*

On controller side, a method annotated with `@LocalStorage` causes the return value to be stored in `localStorage`.
Using @LocalStorage on a method-parameter allows you to read values from `localStorage`.


---

### Using `clientState`

The `clientState` object is similar to `localStorage` but is managed entirely in memory during the current session. It
is not persisted.

```html

<div class="${clientState.sidebarVisible ? 'visible' : 'hidden'}">Sidebar content</div>
```

On controller side, a method annotated with `@ClientState` causes the return value to be kept in memory on the client.
Using @ClientState on a method-parameter allows you to read values previously set in `clientState`.

---

### Example: Shopping Cart with `localStorage` and `clientState`

#### ShoppingPage

```html file="ShoppingPage.html"
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd">
<body>
<div xis:widget-container="shopcontainer" xis:default-widget="ShoppingWidget"/>
<p>You have ${clientState.cartSize} items in your cart.</p>
</body>
</html>
```

```java file="ShoppingPage.java"

@Page("/shop.html")
class ShoppingPage {
}
```

#### ShoppingWidget

```html file="ShoppingWidget.html"

<ul>
    <li xis:repeat="product:products">
        ${product.label}
        <a xis:action="addToCart">
            Add to cart
            <xis:parameter name="productId" value="${product.id}"/>
        </a>
    </li>
</ul>

```

```java file="ShoppingWidget.java"

@Widget
class ShoppingWidget {

    // Static product list as map
    private static final Map<Integer, Product> PRODUCTS = Map.of(
            0, new Product("Book"),
            1, new Product("Headphones"),
            2, new Product("Charger")
    );

    @ModelData
    Map<Integer, Product> products() {
        return PRODUCTS;
    }

    @Action("addToCart")
    @LocalStorage("cart")
    List<Product> addToCart(@LocalStorage("cart") List<Product> cart, @ActionParameter("productId") int id) {
        Product item = PRODUCTS.get(id);
        if (item != null) {
            cart.add(item);
        }
        return cart;
    }

    @ClientState("cartSize")
    int getCartSize(@LocalStorage("cart") List<Product> cart) {
        return cart.size();
    }

    public record Product(int id, String label) {
    }
}
```

This example shows how a reactive cart counter is maintained across sessions using `localStorage` and a minimal
controller.

---

### Summary Table

| Storage Type | Persisted | Accessible in EL | Reactive | Recommended Use                        |
|--------------|-----------|------------------|----------|----------------------------------------|
| localStorage | Yes       | Yes              | Yes      | Persisted preferences, counters, flags |
| clientState  | No        | Yes              | Yes      | UI state, temporary session variables  |

---

### Best Practices

* Keep values **small** and **serializable** (strings, numbers, booleans).
* Prefer `clientState` for ephemeral UI logic.
* Stick to per-component state to support modularity.
* Use `localStorage` when session persistence is necessary (e.g. keeping items in the cart after reload).

> Centralized state management (e.g. Redux-like models) works against the XIS design philosophy of self-contained,
> composable frontend modules.

[← Formatting](13-formatting.md) | [Custom JavaScript and CSS →](15-custom-assets.md)