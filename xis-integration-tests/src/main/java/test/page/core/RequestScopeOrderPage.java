package test.page.core;

import lombok.RequiredArgsConstructor;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.RequestScope;

@Page("/requestScopeOrder.html")
@RequiredArgsConstructor
class RequestScopeOrderPage {

    private final RequestScopeOrderPageService service;

    //
    @RequestScope("a")
    int a() {
        service.record("a");
        return 42;
    }

    //
    @RequestScope("b")
    String b(@RequestScope("a") int id) {
        service.record("b");
        return "abc";
    }

    //
    @ModelData("c")
    @RequestScope("c")
    String c(@RequestScope("b") String token) {
        service.record("c");
        return "model-" + token;
    }

    @ModelData("d")
    String d(@RequestScope("c") String token) {
        service.record("d");
        return "form-" + token;
    }

    @ModelData("xyz")
    @RequestScope("scopedForm")
    String e(@RequestScope("a") int id, @RequestScope("c") String token) {
        service.record("e");
        return "scopedForm-" + id + "-" + token;
    }

}
