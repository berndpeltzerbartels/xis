package test.page.core;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.SharedValue;

import java.util.List;

@Page("/model-data-names.html")
class ModelDataNamePage {

    @ModelData("customer")
    Customer selectedCustomer() {
        return new Customer("explicit");
    }

    @ModelData
    Customer methodCustomer() {
        return new Customer("method");
    }

    @ModelData
    Customer getGetterCustomer() {
        return new Customer("getter");
    }

    @SharedValue("customers")
    List<Customer> customers() {
        return List.of(new Customer("pipeline"));
    }

    @ModelData
    List<Customer> getPipelineCustomers(@SharedValue("customers") List<Customer> customers) {
        return customers;
    }

    @ModelData
    Customer get() {
        return new Customer("plain-get");
    }

    @ModelData
    Customer gettyImages() {
        return new Customer("getty");
    }

    record Customer(String name) {
    }
}
