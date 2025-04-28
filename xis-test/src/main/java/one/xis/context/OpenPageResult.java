package one.xis.context;

import lombok.Data;
import one.xis.test.dom.Document;
import one.xis.test.dom.Window;
import one.xis.test.js.Console;
import one.xis.test.js.LocalStorage;

@Data
public class OpenPageResult {
    private final AppContext appContext;
    private final IntegrationTestEnvironment testEnvironment;

    public Document getDocument() {
        return testEnvironment.getHTML_OBJECTS().getRootPage();
    }

    public LocalStorage getLocalStorage() {
        return testEnvironment.getHTML_OBJECTS().getLocalStorage();
    }

    public Window getWindow() {
        return testEnvironment.getHTML_OBJECTS().getWindow();
    }

    public Console getConsole() {
        return testEnvironment.getHTML_OBJECTS().getConsole();
    }
}


