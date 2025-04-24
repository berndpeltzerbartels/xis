package test.page.core;

import lombok.RequiredArgsConstructor;
import one.xis.*;

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

    //
    @FormData("token")
    String form(@RequestScope("c") String token) {
        service.record("c");
        return "form-" + token;
    }

    //
    @FormData("form")
    @RequestScope("scopedForm")
    String d(@RequestScope("a") int id, @RequestScope("c") String token) {
        service.record("d");
        return "scopedForm-" + id + "-" + token;
    }

    @Action("go")
    void go(@RequestScope("a") int id, @RequestScope("b") String token) {
        service.record("go");
    }
}
