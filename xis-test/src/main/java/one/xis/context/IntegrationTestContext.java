package one.xis.context;


import one.xis.resource.Resources;
import one.xis.server.PageUtil;
import one.xis.test.dom.Document;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

public class IntegrationTestContext {

    private static IntegrationTestEnvironment environment;
    private final AppContext appContext;
    private final Resources resources;
    private static final Object SYNC_LOCK = new Object();

    public static Builder builder() {
        return new Builder();
    }

    IntegrationTestContext(Object... controllers) {
        this.resources = new Resources();
        this.appContext = internalContext(controllers);
    }

    public void openPage(String uri, Map<String, Object> parameters) {
        synchronized (SYNC_LOCK) {
            if (environment == null) {
                // loads page as initial page
                environment = new IntegrationTestEnvironment(resources.getByPath("xis.js"), resources.getByPath("index.html"), uri);
            }
            environment.reset();
            environment.openPage(uri, appContext);
            environment.afterPageLoaded();
        }

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
