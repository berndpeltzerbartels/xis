# Refresh Events

Refresh events let server-side application code tell already open browser clients that a page or frontlet should reload
its model data and render again. Use them for UI state that can change outside the current browser action, for example a
cart counter, a dashboard tile, a notification badge, or a score display.

This is not an SSO feature. Authentication can be involved when you target a user, but the mechanism itself is a
general UI refresh mechanism.

## Declare The Refresh Key

Put `@RefreshOnUpdateEvents` on every page or frontlet that should react to an event key.

```java
import one.xis.ModelData;
import one.xis.Page;
import one.xis.RefreshOnUpdateEvents;

@Page("/cart.html")
@RefreshOnUpdateEvents("cart-updated")
public class CartPage {

    private final CartService cartService;

    public CartPage(CartService cartService) {
        this.cartService = cartService;
    }

    @ModelData("itemCount")
    int itemCount() {
        return cartService.currentItemCount();
    }
}
```

```html
<span id="cart-count">${itemCount}</span>
```

When the browser receives `cart-updated`, XIS reloads the model data for the current page if that page declares the key.
If the page does not declare it, XIS checks the currently visible frontlets and refreshes those whose controller declares
the key.

Event keys must be declared. Publishing an unknown key is treated as a programming error, because otherwise a typo would
look like a refresh that simply never arrives.

## Page Content Or Frontlet

A value such as a cart counter can live directly in the page template:

```java
@Page("/products.html")
@RefreshOnUpdateEvents("cart-updated")
public class ProductsPage {

    private final CartService cartService;

    public ProductsPage(CartService cartService) {
        this.cartService = cartService;
    }

    @ModelData("cartItemCount")
    int cartItemCount() {
        return cartService.currentItemCount();
    }
}
```

```html
<header>
    <span id="cart-count">${cartItemCount}</span>
</header>
```

That is fine code style when the value belongs to the page and a page refresh is acceptable. Every page that renders the
counter directly must declare the refresh key and expose the model data.

If the same counter should be refreshed as an independent fragment across pages, model it as a frontlet instead:

```java
import one.xis.Frontlet;
import one.xis.ModelData;
import one.xis.RefreshOnUpdateEvents;

@Frontlet
@RefreshOnUpdateEvents("cart-updated")
public class CartSummaryFrontlet {

    private final CartService cartService;

    public CartSummaryFrontlet(CartService cartService) {
        this.cartService = cartService;
    }

    @ModelData("cartItemCount")
    int cartItemCount() {
        return cartService.currentItemCount();
    }
}
```

```html
<xis:frontlet-container container-id="cart-summary" default-frontlet="CartSummaryFrontlet"/>
```

XIS does not provide a targeted partial reload for arbitrary page markup. A targeted fragment refresh needs a frontlet.
Do not use `PageResponse` from a frontlet action just to force the whole page to reload for a header counter; that makes
the action depend on page navigation when the actual intent is only to refresh a small UI fragment.

## Publish To All Clients

Inject `RefreshEventPublisher` into a service or controller and publish the declared key.

```java
import one.xis.Action;
import one.xis.Page;
import one.xis.server.RefreshEventPublisher;

@Page("/cart.html")
public class CartPage {

    private final CartService cartService;
    private final RefreshEventPublisher refreshEvents;

    public CartPage(CartService cartService, RefreshEventPublisher refreshEvents) {
        this.cartService = cartService;
        this.refreshEvents = refreshEvents;
    }

    @Action
    void addItem() {
        cartService.addItem();
        refreshEvents.publishToAll("cart-updated");
    }
}
```

`publishToAll(...)` sends the event to every connected client. Use this for public or shared state such as a scoreboard.

## Publish To One Client

Use `@ClientId` when only the browser client that triggered an action should refresh.

```java
import one.xis.Action;
import one.xis.ClientId;
import one.xis.Page;
import one.xis.server.RefreshEventPublisher;

@Page("/cart.html")
public class CartPage {

    private final RefreshEventPublisher refreshEvents;

    public CartPage(RefreshEventPublisher refreshEvents) {
        this.refreshEvents = refreshEvents;
    }

    @Action
    void recalculate(@ClientId String clientId) {
        refreshEvents.publishToClient("cart-updated", clientId);
    }
}
```

A client is one browser connection. Two tabs or two devices of the same logged-in user are different clients.

## Publish To One User

Use `publishToUser(...)` when every connected client of the same authenticated user should refresh.

```java
import one.xis.Action;
import one.xis.Page;
import one.xis.RefreshOnUpdateEvents;
import one.xis.Roles;
import one.xis.UserId;
import one.xis.server.RefreshEventPublisher;

@Page("/account/cart.html")
@Roles("USER")
@RefreshOnUpdateEvents("cart-updated")
public class UserCartPage {

    private final CartService cartService;
    private final RefreshEventPublisher refreshEvents;

    public UserCartPage(CartService cartService, RefreshEventPublisher refreshEvents) {
        this.cartService = cartService;
        this.refreshEvents = refreshEvents;
    }

    @Action
    void addItem(@UserId String userId) {
        cartService.addItem(userId);
        refreshEvents.publishToUser("cart-updated", userId);
    }
}
```

User targeting requires authentication, because XIS must know which connected clients belong to which user. It is not the
same as client targeting: one user can have several clients.

## Publish To Authenticated Users

Use `publishToAllUsers(...)` when every authenticated client should refresh, but anonymous clients should not.

```java
@Action
void publishAccountNotice() {
    refreshEvents.publishToAllUsers("account-notice-updated");
}
```

Use this for state that only makes sense after login. Use `publishToAll(...)` when anonymous clients should also receive
the refresh.

## Reload Other Frontlets After An Action

`FrontletResponse.reloadFrontlet(...)` is a related action-response feature. It does not broadcast to other clients. It
reloads matching frontlets in the current browser after the action completes.

This is a special case. You do not need it to refresh the page or frontlet that handled the action:

- A page action refreshes the current page response.
- A frontlet action refreshes the current frontlet response.
- A child frontlet is refreshed when its parent frontlet is refreshed.

Use `reloadFrontlet(...)` only when an action should also refresh another already visible frontlet that is not refreshed
through that normal action response. A common example is a separate cart-summary frontlet in the page header while the
action itself runs in a product-list or cart-editor frontlet.

```java
import one.xis.Action;
import one.xis.Frontlet;
import one.xis.FrontletResponse;

@Frontlet
public class CartEditorFrontlet {

    @Action
    FrontletResponse save() {
        return new FrontletResponse()
                .reloadFrontlet("CartSummaryFrontlet");
    }
}
```

If the current page contains a `CartSummaryFrontlet`, XIS reloads that frontlet's model data and renders it again. The
frontlet can be in another container. You do not need this call for `CartEditorFrontlet` itself; the action response
already updates the frontlet that handled the action.

## Choosing The Right Target

| Need | Use |
| --- | --- |
| Public/shared state changed for every open browser | `publishToAll("key")` |
| Only the current browser client should refresh | `publishToClient("key", clientId)` with `@ClientId` |
| All clients of one logged-in user should refresh | `publishToUser("key", userId)` with `@UserId` |
| All authenticated clients should refresh | `publishToAllUsers("key")` |
| Only the current browser should reload one visible frontlet after an action | `new FrontletResponse().reloadFrontlet("FrontletId")` |
