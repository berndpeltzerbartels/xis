package one.xis.context;

import lombok.Data;
import one.xis.test.dom.Document;

@Data
public class OpenPageResult {
    private final AppContext appContext;
    private final IntegrationTestEnvironment testEnvironment;

    public Document getDocument() {
        return testEnvironment.getHTML_OBJECTS().getRootPage();
    }
}


