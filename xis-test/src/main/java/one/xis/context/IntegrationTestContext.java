package one.xis.context;


import one.xis.resource.Resources;
import one.xis.server.PageUtil;
import one.xis.test.dom.Document;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

public class IntegrationTestContext {

    private final AppContext appContext;
    private final IntegrationTestEnvironment environment;

    public static Builder builder() {
        return new Builder();
    }

    IntegrationTestContext(Object... controllers) {
        this.appContext = internalContext(controllers);
        this.environment = new IntegrationTestEnvironment();
    }

    public void openPage(String uri, Map<String, Object> parameters) {
        environment.openPage(uri, appContext);
    }

    public Document getDocument() {
        return environment.getHtmlObjects().getRootPage();
    }

    public void openPage(String uri) {
        openPage(uri, Collections.emptyMap());
    }


    public void openPage(Class<?> pageController) {
        openPage(PageUtil.getUrl(pageController), Collections.emptyMap());
    }


    public <T> T getSingleton(Class<T> type) {
        return appContext.getSingleton(type);
    }

    private AppContext internalContext(Object... controllers) {
        return AppContextBuilder.createInstance()
                .withPackage("one.xis")
                .withSingletons(controllers)
                .build();
    }

    @SuppressWarnings("unused")
    public static class Builder {

        private final Collection<Object> singletons = new HashSet<>();
        private static IntegrationTestEnvironment testSingletons;
        private static final Resources RESOURCES = new Resources();

        public Builder withSingleton(Object o) {
            singletons.add(o);
            return this;
        }

        public Builder withMock(Object o) {
            return withSingleton(o);
        }

        public IntegrationTestContext build() {
            return new IntegrationTestContext(singletons.toArray());
        }

    }


}
