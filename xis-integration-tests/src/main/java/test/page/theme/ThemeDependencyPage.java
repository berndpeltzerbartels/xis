package test.page.theme;

import one.xis.FormData;
import one.xis.Page;
import one.xis.Action;

@Page("/theme-dependency.html")
class ThemeDependencyPage {

    @FormData("customer")
    CustomerForm customer() {
        var customer = new CustomerForm();
        customer.name = "Ada";
        return customer;
    }

    @Action
    void save(@FormData("customer") CustomerForm customer) {
    }

    static class CustomerForm {
        String name;
    }
}
