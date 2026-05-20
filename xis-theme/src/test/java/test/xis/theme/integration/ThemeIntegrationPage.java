package test.xis.theme.integration;

import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.Page;

import java.util.List;

@Page("/theme-integration.html")
class ThemeIntegrationPage {

    private CustomerForm savedCustomer;

    @FormData("customer")
    CustomerForm customer() {
        var customer = new CustomerForm();
        customer.name = "Ada";
        customer.stage = "LEAD";
        return customer;
    }

    @ModelData("stages")
    List<StageOption> stages() {
        return List.of(
                new StageOption("LEAD", "Lead"),
                new StageOption("CUSTOMER", "Customer")
        );
    }

    @Action
    void save(@FormData("customer") CustomerForm customer) {
        savedCustomer = customer;
    }

    CustomerForm savedCustomer() {
        return savedCustomer;
    }

    static class CustomerForm {
        String name;
        String stage;
    }

    record StageOption(String code, String label) {
    }
}
