package test.page.core;

import lombok.RequiredArgsConstructor;
import one.xis.MethodParameter;
import one.xis.ModelData;
import one.xis.Page;

@Page("/requestScopeOrder.html")
@RequiredArgsConstructor
class MethodParameterOrderPage {

    private final MethodParameterOrderPageService service;

    //
    @MethodParameter("a")
    int a() {
        service.record("a");
        return 42;
    }

    //
    @MethodParameter("b")
    String b(@MethodParameter("a") int id) {
        service.record("b");
        return "abc";
    }

    //
    @ModelData("c")
    @MethodParameter("c")
    String c(@MethodParameter("b") String token) {
        service.record("c");
        return "model-" + token;
    }

    @ModelData("d")
    String d(@MethodParameter("c") String token) {
        service.record("d");
        return "form-" + token;
    }

    @ModelData("xyz")
    @MethodParameter("scopedForm")
    String e(@MethodParameter("a") int id, @MethodParameter("c") String token) {
        service.record("e");
        return "scopedForm-" + id + "-" + token;
    }

}
