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
        return testEnvironment.getHTML_OBJECTS().getDocument();
    }

    public LocalStorage getLocalStorage() {
        return testEnvironment.getHTML_OBJECTS().getLocalStorage();
    }

    public SessionStorage getSessionStorage() {
        return testEnvironment.getHTML_OBJECTS().getSessionStorage();
    }

    public Window getWindow() {
        return testEnvironment.getHTML_OBJECTS().getWindow();
    }

    /*
    public Console getConsole() {
        return testEnvironment.getHTML_OBJECTS().getConsole();
    }

     */
}


