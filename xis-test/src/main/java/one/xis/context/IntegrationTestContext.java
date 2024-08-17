package one.xis.context;


import lombok.Getter;
import one.xis.resource.Resources;
import one.xis.server.PageUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class IntegrationTestContext {

    @Getter
    private final AppContext appContext;
    private final IntegrationTestEnvironment environment;

    private static final Object SYNC_LOCK = new Object();

    public static Builder builder() {
        return new Builder();
    }

    IntegrationTestContext(Collection<String> packages, Object... controllers) {
        this.appContext = internalContext(packages, controllers);
        this.environment = new IntegrationTestEnvironment(appContext.getSingleton(BackendBridge.class));
    }

    public IntegrationTestResult openPage(String uri, Map<String, Object> parameters) {
        synchronized (SYNC_LOCK) {
            if (!parameters.isEmpty()) {
                uri += "?";
                uri += parameters.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
            }
            environment.openPage(uri);
            return new IntegrationTestResult(appContext, environment);
        }
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

    private AppContext internalContext(Collection<String> packages, Object... controllers) {
        var builder = AppContextBuilder.createInstance()
                .withXIS()
                .withSingletons(controllers);
        packages.forEach(builder::withPackage);
        return builder.build();
    }

    @SuppressWarnings("unused")
    public static class Builder {

        private final Collection<Object> singletons = new HashSet<>();
        private final Collection<String> packages = new HashSet<>();
        private final Collection<String> ignorePackages = new HashSet<>();

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
            return new IntegrationTestContext(packages, singletons.toArray());
        }

        public Builder withPackage(String packageName) {
            packages.add(packageName);
            return this;
        }

        public Builder withBasePackageClass(Class<?> type) {
            packages.add(type.getPackageName());
            return this;
        }

        public Builder withoutPackage(String packageName) {
            ignorePackages.add(packageName);
            return this;
        }

        public Builder withoutBasePackageClass(Class<?> type) {
            ignorePackages.add(type.getPackageName());
            return this;
        }
    }


}
