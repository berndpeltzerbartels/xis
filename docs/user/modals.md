# Modals

[Documentation map](../README.md)

Use a modal when the user should edit or confirm something without leaving the current page context. A modal is a normal
XIS controller with its own template fragment, model data, form data, actions, validation, and parameters.

## Define A Modal

```java
import one.xis.Action;
import one.xis.FormData;
import one.xis.Parameter;
import one.xis.Modal;
import one.xis.ModalResponse;
import one.xis.validation.Mandatory;

@Modal("/customers/edit")
class EditCustomerModal {

    static class CustomerForm {
        @Mandatory
        public String name;
    }

    @FormData("customer")
    CustomerForm customer() {
        return new CustomerForm();
    }

    @Action
    ModalResponse save(@FormData("customer") CustomerForm customer) {
        customerService.save(customer);
        return ModalResponse.close().reloadParent();
    }
}
```

`EditCustomerModal.html`

```html
<div xmlns:xis="https://xis.one/xsd">
  <h2>Edit customer</h2>

  <xis:form binding="customer">
    <label for="customer-name">Name</label>
    <input id="customer-name" xis:binding="name" xis:error-class="error"/>
    <xis:message message-for="name"/>

    <button xis:action="save">Save</button>
  </xis:form>
</div>
```

The template is a fragment, not a full HTML document.

## Open From HTML

Use `xis:modal` on a button or link when the click can open the modal directly. This is the normal case.

```html
<button xis:modal="EditCustomerModal">Edit</button>
```

Pass modal parameters with `xis:parameter`. The modal receives them with `@Parameter`.

```html
<button xis:modal="EditCustomerModal">
  <xis:parameter name="customerId" value="${customer.id}"/>
  Edit
</button>
```

```java
@FormData("customer")
CustomerForm customer(@Parameter("customerId") long customerId) {
    return customerService.form(customerId);
}
```

You can also open a modal by a simple modal path such as `xis:modal="/customers/edit"`. For dynamic values, prefer the
modal id plus `xis:parameter`; it is easier to read and does not make the client guess route variables.

Modal paths may also contain query parameters:

```html
<button xis:modal="/customers/edit?customerId=${customer.id}">Edit</button>
```

These values are modal parameters, not page URL query parameters, so the modal reads them with `@Parameter`.

## Open Or Close From Java

Actions can return `ModalResponse`. Use this when the server must decide which modal to open, when it must compute
parameters first, or when an action inside a modal should close it. If a button or link only opens a known modal, prefer
`xis:modal`; it keeps the template easier to read and avoids an unnecessary action method.

```java
@Action
ModalResponse edit(@Parameter("customerId") long customerId) {
    return ModalResponse.open(EditCustomerModal.class)
            .parameter("customerId", customerId);
}
```

`ModalResponse.open("/customers/edit?customerId=42")` follows the same rule: the query string is available in the modal
as `@Parameter("customerId")`.

Return `void` when the action should stay in the modal and simply refresh its data. Return `ModalResponse` when the
action should open another modal, close the current modal, or close it and reload the parent UI.

Close without reloading the parent:

```java
@Action
ModalResponse cancel() {
    return ModalResponse.close();
}
```

Close and reload the parent:

```java
@Action
ModalResponse save(@FormData("customer") CustomerForm customer) {
    customerService.save(customer);
    return ModalResponse.close().reloadParent();
}
```

If the modal was opened from a page, `reloadParent()` reloads that page. If it was opened from a frontlet, XIS reloads
exactly the frontlet instance that opened the modal. This matters for lists where every row is the same frontlet type:
saving one row dialog should not reload every row.

## Validation

Validation works like normal XIS forms. If validation fails, the modal stays open, the action method is not called, and
the validation messages are rendered inside the modal.

## When To Use Events Instead

`ModalResponse.close().reloadParent()` is for the current browser and the immediate parent UI. Use refresh events when a
change should update other browsers, other users, or unrelated visible parts of the page.

## Tests

Modal behavior is covered in the E2E suite. See `ModalE2ETest` in the end-to-end test repository for examples that open a
modal from HTML, open one from an action response, validate a modal form, close it, and reload only the opening frontlet
instance.
