package one.xis.context;

import lombok.Getter;
import one.xis.test.dom.Document;
import one.xis.test.dom.Window;
import one.xis.test.js.LocalStorage;
import one.xis.test.js.SessionStorage;

@Getter
public class TestClient {
    private final AppContext appContext;
    private final IntegrationTestEnvironment testEnvironment;

    TestClient(AppContext appContext, IntegrationTestEnvironment testEnvironment) {
        this.appContext = appContext;
        this.testEnvironment = testEnvironment;
    }

    public Document getDocument() {
        return testEnvironment.getHtmlObjects().getDocument();
    }

    public LocalStorage getLocalStorage() {
        return testEnvironment.getHtmlObjects().getLocalStorage();
    }

    public SessionStorage getSessionStorage() {
        return testEnvironment.getHtmlObjects().getSessionStorage();
    }

    public Window getWindow() {
        return testEnvironment.getHtmlObjects().getWindow();
    }
}
