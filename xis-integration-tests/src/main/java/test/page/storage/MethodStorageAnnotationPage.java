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

    @Action("clear-all")
    @LocalStorage("localSimple")
    @SessionStorage("sessionSimple")
    @ClientState("clientSimple")
    String clearAll() {
        return null;
    }
}
