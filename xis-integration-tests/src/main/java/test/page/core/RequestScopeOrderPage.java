package test.page.core;

import lombok.RequiredArgsConstructor;
import one.xis.*;

@Page("/requestScopeOrder.html")
@RequiredArgsConstructor
class RequestScopeOrderPage {

    private final RequestScopeOrderPageService service;

    //
    @RequestScope("id")
    int provideId() {
        service.record("provideId");
        return 42;
    }

    //
    @RequestScope("token")
    String provideToken() {
        service.record("provideToken");
        return "abc";
    }

    //
    @ModelData("normalModel")
    String normalModel() {
        service.record("normalModel");
        return "foo";
    }

    //
    @ModelData("model")
    @RequestScope("model")
    String requestScopedModel(@RequestScope("id") int id) {
        service.record("requestScopedModel: id=" + id);
        return "model-" + id;
    }

    //
    @FormData("token")
    String form(@RequestScope("token") String token) {
        service.record("form: token=" + token);
        return "form-" + token;
    }

    //
    @FormData("form")
    @RequestScope("scopedForm")
    String scopedForm(@RequestScope("id") int id, @RequestScope("token") String token) {
        service.record("scopedForm: id=" + id + ", token=" + token);
        return "scopedForm-" + id + "-" + token;
    }

    @Action("go")
    void go(@RequestScope("id") int id, @RequestScope("token") String token) {
        service.record("go: id=" + id + ", token=" + token);
    }
}
