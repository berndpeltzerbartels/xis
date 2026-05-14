package test.page.core;

import lombok.RequiredArgsConstructor;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.SharedValue;

@Page("/requestScopeOrder.html")
@RequiredArgsConstructor
class MethodParameterOrderPage {

    private final MethodParameterOrderPageService service;

    //
    @SharedValue("a")
    int a() {
        service.record("a");
        return 42;
    }

    //
    @SharedValue("b")
    String b(@SharedValue("a") int id) {
        service.record("b");
        return "abc";
    }

    //
    @ModelData("c")
    @SharedValue("c")
    String c(@SharedValue("b") String token) {
        service.record("c");
        return "model-" + token;
    }

    @ModelData("d")
    String d(@SharedValue("c") String token) {
        service.record("d");
        return "form-" + token;
    }

    @ModelData("xyz")
    @SharedValue("scopedForm")
    String e(@SharedValue("a") int id, @SharedValue("c") String token) {
        service.record("e");
        return "scopedForm-" + id + "-" + token;
    }

}
