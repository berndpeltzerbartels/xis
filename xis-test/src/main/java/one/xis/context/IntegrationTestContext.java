package one.xis.context;


import one.xis.resource.Resources;
import one.xis.server.PageUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

public class IntegrationTestContext {

    private final AppContext appContext;
    private final IntegrationTestEnvironment environment;

    private static final Object SYNC_LOCK = new Object();

    public static Builder builder() {
        return new Builder();
    }

    IntegrationTestContext(Object... controllers) {
        this.appContext = internalContext(controllers);
        this.environment = new IntegrationTestEnvironment();
    }

    public IntegrationTestResult openPage(String uri, Map<String, Object> parameters) {
        //synchronized (SYNC_LOCK) {
        environment.openPage(uri, appContext);
        return new IntegrationTestResult(appContext, environment);
        //}
    }

    public IntegrationTestResult openPage(String uri) {
        return openPage(uri, Collections.emptyMap());
    }


    public IntegrationTestResult openPage(Class<?> pageController) {
        return openPage(PageUtil.getUrl(pageController), Collections.emptyMap());
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
