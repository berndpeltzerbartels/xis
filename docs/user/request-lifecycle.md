# Request Lifecycle

[Documentation map](../README.md)

This chapter gives you the mental model for when XIS calls controller methods and how a browser interaction becomes a
new rendered page or frontlet.

## Initial Page Load

When the browser opens a page URL, XIS matches the URL to a `@Page` controller.

```java
@Page("/products/{id}.html")
class ProductPage {

    @ModelData("product")
    Product product(@PathVariable("id") long id) {
        return productService.findById(id);
    }
}
```

The initial load runs the page data methods, evaluates template expressions, processes XIS tags and attributes, and
renders the HTML document. If the page contains frontlet containers, the configured frontlets are loaded as part of that
rendering flow.

## Navigation Without Actions

Normal page and frontlet links do not call an action method. They only tell XIS what should be displayed next.

```html
<a xis:page="/products/42.html">Open product</a>

<xis:frontlet-container container-id="details"
                        default-frontlet="ProductDetails"/>
```

Use navigation links when no server-side business method needs to run.

## Actions

An action is a user-triggered call to a Java method annotated with `@Action`.

```html
<button xis:action="delete">
    <xis:parameter name="productId" value="${product.id}"/>
    Delete
</button>
```

```java
@Action
PageResponse delete(@ActionParameter("productId") long productId) {
    productService.delete(productId);
    return new PageResponse(ProductListPage.class);
}
```

The action return type decides what happens next:

| Return type | Result |
| --- | --- |
| `void` | Stay on the current page or frontlet and refresh its data. |
| `Class<?>` | Navigate to that page, or replace the current frontlet when the class is a frontlet. |
| `String` | Navigate to the URL string. |
| `PageResponse` / `PageUrlResponse` | Navigate to a page with explicit path variables, query parameters, or URL details. |
| `FrontletResponse` | Replace a frontlet, optionally in a specific container. |
| `ModalResponse` | Open or close a modal dialog. |

For the full matrix, including container rules, frontlet-to-frontlet replacement, and modal responses, see
[Navigation and responses](navigation.md).

`@Action` is also a lifecycle filter. A method annotated with `@Action` runs only when that exact action is triggered,
no matter which other XIS annotations are present on the same method. For example, `@Action @ModelData` is not part of
the initial model load, and `@Action @FormData` is not a normal form initializer. The additional annotation only tells
XIS how to use the return value after the action has actually run.

An action method may also be annotated with `@ModelData`. In that case the action still runs because the user triggered
it, and its return value is also written into the model data of the current response. This is useful for small UI
results that should appear immediately without adding another model method.

```html
<button xis:action="calculateDiscount">
    <xis:parameter name="productId" value="${product.id}"/>
    Calculate discount
</button>

<p>${discountMessage}</p>
```

```java
@Action
@ModelData("discountMessage")
String calculateDiscount(@ActionParameter("productId") long productId) {
    return discountService.discountMessageFor(productId);
}
```

If an action method and a regular model method both write the same model key, the action result is kept for the current
response. This lets an action replace an initial default value without being overwritten by the default model method.

```java
@ModelData(varName = "selectedStepId", load = ModelDataLoad.INITIAL)
Long selectedStepId() {
    return pipelineService.firstStepId().orElse(null);
}

@Action
@ModelData("selectedStepId")
Long selectStep(@ActionParameter("stepId") long stepId) {
    return stepId;
}
```

Use `ModelDataLoad.INITIAL` for values that should only be calculated when a page or frontlet is opened. Use
`ModelDataLoad.AFTER_ACTION` for values that should only be calculated while rendering the same page or frontlet after
an action. `ModelDataLoad.ALWAYS` is the default. The same `load` attribute is available on `@FormData` methods that
initialize forms.

### Initial Model Data For Default Selection

A common frontlet pattern is a list with a detail area: when the frontlet is opened, the first item should be selected.
After the user clicks another item, that explicit choice must win and must not be replaced by the initial default again.

```html
<button xis:repeat="step:pipelineSteps"
        xis:action="selectStep"
        type="button">
    <xis:parameter name="stepId" value="${step.id}"/>
    ${step.name}
</button>

<section>${selectedStep.name}</section>
```

```java
@Frontlet
class PipelineFrontlet {
    private final PipelineService pipelineService;

    PipelineFrontlet(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @SharedValue("pipelineSteps")
    List<PipelineStep> pipelineSteps(@FrontletParameter("pipelineId") long pipelineId) {
        return pipelineService.stepsForPipeline(pipelineId);
    }

    @ModelData(varName = "pipelineSteps")
    List<PipelineStep> pipelineStepsModel(@SharedValue("pipelineSteps") List<PipelineStep> pipelineSteps) {
        return pipelineSteps;
    }

    @ModelData(varName = "selectedStep", load = ModelDataLoad.INITIAL)
    PipelineStep initiallySelectedStep(@SharedValue("pipelineSteps") List<PipelineStep> pipelineSteps) {
        return pipelineSteps.isEmpty() ? null : pipelineSteps.get(0);
    }

    @Action
    @ModelData(varName = "selectedStep")
    PipelineStep selectStep(@ActionParameter("stepId") long stepId,
                            @SharedValue("pipelineSteps") List<PipelineStep> pipelineSteps) {
        return pipelineSteps.stream()
                .filter(step -> step.id() == stepId)
                .findFirst()
                .orElseThrow();
    }
}
```

The shared value keeps the list loading in one place for the current request. `INITIAL` chooses the default only when the
frontlet is opened. The action return value uses the same model key and therefore replaces the default selection in the
current response.

## Forms

A form action adds binding and validation before the action method runs.

```html
<form xis:binding="product">
    <input xis:binding="name">
    <button xis:action="save">Save</button>
</form>
```

```java
@Action
PageResponse save(@FormData("product") ProductForm product) {
    productService.save(product);
    return new PageResponse(ProductDetailsPage.class)
            .pathVariable("id", product.id());
}
```

XIS deserializes submitted values into the `@FormData` object and validates its annotations. If validation fails, the
action method is not called. The page or frontlet is rendered again with the submitted values and validation messages.

Like `@ModelData`, form initialization can be limited to a lifecycle phase. This is useful when an initial form should
be built from the current selection, but after a successful action the form should show a fresh post-action state instead
of being overwritten by the initial default again.

```java
@FormData(value = "step", load = ModelDataLoad.INITIAL)
PipelineStepForm initiallySelectedStep(@SharedValue("selectedStep") PipelineStep selectedStep) {
    return PipelineStepForm.from(selectedStep);
}

@FormData(value = "step", load = ModelDataLoad.AFTER_ACTION)
PipelineStepForm emptyStepForm(@SharedValue("pipeline") Pipeline pipeline) {
    return PipelineStepForm.newStep(pipeline.id());
}
```

An action can also return the form data for the next render by combining `@Action` and `@FormData`. The returned value
is used for that form directly; XIS does not issue a second form-model request for the same form binding.
Because `@Action` is a lifecycle filter, this method is not called when XIS initializes the form. It runs only when
`selectStep` is triggered.

```java
@Action
@FormData("step")
PipelineStepForm selectStep(@ActionParameter("stepId") long stepId) {
    return PipelineStepForm.from(pipelineService.step(stepId));
}
```

See [Forms and validation](forms-and-validation.md) for message rendering, custom validators, records, and formatters.

## Shared Values

Use `@SharedValue` when several methods in the same processing flow need the same object.

```java
@SharedValue("product")
Product product(@PathVariable("id") long id) {
    return productService.findById(id);
}

@ModelData("product")
Product productModel(@SharedValue("product") Product product) {
    return product;
}

@Action
void rename(@SharedValue("product") Product product,
            @ActionParameter("name") String name) {
    product.rename(name);
}
```

This avoids repeating database or service lookups and lets an action work with the same contextual object that was
loaded for rendering.
Shared-value provider methods must not be annotated with `@Action`. They are dependency providers and run whenever a
method in the same processing flow needs their value.

## Client State

`@LocalStorage`, `@SessionStorage`, and `@ClientState` read named values that XIS knows the controller may need. XIS
does not send the whole browser state. It scans controller annotations and only transfers keys that may be used by the
current controller.

```java
@Action
void addToCart(@LocalStorage("cart") Cart cart,
               @ActionParameter("productId") String productId) {
    cart.add(productId);
}
```

Prefer server-side state for normal application data. Client state is a convenience feature for browser-local state such
as a cart draft, wizard state, or UI preferences.

## Server-Triggered Refresh Events

The normal action response updates the current page or frontlet. Refresh events are for other already open pages or
frontlets that should reload because shared state changed.

To publish one, inject `RefreshEventPublisher` and call `publishToAll(...)`, `publishToClient(...)`,
`publishToUser(...)`, or `publishToAllUsers(...)`.

```java
@Action
void addItem(@ClientId String clientId) {
    cartService.add(clientId);
    refreshEventPublisher.publishToClient("cart-updated", clientId);
}
```

See [Events](events.md) for the full publishing examples, target rules, and
frontlet reload details.
