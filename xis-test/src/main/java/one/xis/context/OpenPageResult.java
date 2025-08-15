package one.xis.context;

import lombok.Data;
import one.xis.test.dom.Document;
import one.xis.test.dom.Window;
import one.xis.test.js.LocalStorage;
import one.xis.test.js.SessionStorage;

@Data
public class OpenPageResult {
    private final AppContext appContext;
    private final IntegrationTestEnvironment testEnvironment;

    public Document getDocument() {
        return testEnvironment.getHtmlObjects().getDocument().getDocument();
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

    /*
    public Console getConsole() {
        return testEnvironment.getHTML_OBJECTS().getConsole();
    }

     */
}


