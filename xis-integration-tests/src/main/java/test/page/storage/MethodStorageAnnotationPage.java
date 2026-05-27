package test.page.storage;

import one.xis.Action;
import one.xis.ClientState;
import one.xis.LocalStorage;
import one.xis.Page;
import one.xis.SessionStorage;

@Page("/method-storage-annotation.html")
class MethodStorageAnnotationPage {

    @LocalStorage("localSimple")
    String localSimple() {
        return "local-start";
    }

    @SessionStorage("sessionSimple")
    String sessionSimple() {
        return "session-start";
    }

    @ClientState("clientSimple")
    String clientSimple() {
        return "client-start";
    }

    @LocalStorage("localComplex")
    MethodStorageData localComplex() {
        return new MethodStorageData("local-kept", "local-removed");
    }

    @SessionStorage("sessionComplex")
    MethodStorageData sessionComplex() {
        return new MethodStorageData("session-kept", "session-removed");
    }

    @ClientState("clientComplex")
    MethodStorageData clientComplex() {
        return new MethodStorageData("client-kept", "client-removed");
    }

    @Action("clear-all")
    @LocalStorage("localSimple")
    @SessionStorage("sessionSimple")
    @ClientState("clientSimple")
    String clearAll() {
        return null;
    }

    @Action("clear-complex-fields")
    void clearComplexFields(@LocalStorage("localComplex") MethodStorageData localComplex,
                            @SessionStorage("sessionComplex") MethodStorageData sessionComplex,
                            @ClientState("clientComplex") MethodStorageData clientComplex) {
        localComplex.setRemoved(null);
        sessionComplex.setRemoved(null);
        clientComplex.setRemoved(null);
    }
}
