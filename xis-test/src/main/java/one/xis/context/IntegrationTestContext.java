package one.xis.context;


import lombok.Getter;
import one.xis.auth.UserInfo;
import one.xis.auth.token.TokenService;
import one.xis.server.PageUtil;

import java.util.*;
import java.util.stream.Collectors;

public class IntegrationTestContext implements AppContext {

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

    public OpenPageResult openPage(String uri, Map<String, Object> parameters) {
        synchronized (SYNC_LOCK) {
            if (!parameters.isEmpty()) {
                uri += "?";
                uri += parameters.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
            }
            environment.openPage(uri);
            return new OpenPageResult(appContext, environment);
        }
    }

    public OpenPageResult openPage(String uri) {
        return openPage(uri, Collections.emptyMap());
    }


    public OpenPageResult openPage(Class<?> pageController) {
        return openPage(PageUtil.getUrl(pageController), Collections.emptyMap());
    }


    public <T> T getSingleton(Class<T> type) {
        return appContext.getSingleton(type);
    }

    public <T> Optional<T> getOptionalSingleton(Class<T> type) {
        return appContext.getOptionalSingleton(type);
    }

    @Override
    public Collection<Object> getSingletons() {
        return appContext.getSingletons();
    }

    @Override
    public Collection<Object> getSingletons(Class<?> type) {
        return appContext.getSingletons();
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
        private UserInfo userInfo;

        public Builder withSingleton(Object o) {
            singletons.add(o);
            return this;
        }

        public Builder withMock(Object o) {
            return withSingleton(o);
        }

        public IntegrationTestContext build() {
            var context = new IntegrationTestContext(packages, singletons.toArray());
            if (userInfo != null) {
                addTokens(userInfo, context);
            }
            context.environment.getIntegrationTestScript().reset();
            return context;
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

        public Builder withLoggedInUser(UserInfo userInfo) {
            this.userInfo = userInfo;
            return this;
        }

        private static void addTokens(UserInfo userInfo, IntegrationTestContext context) {
            System.err.println("Adding token cookies for user: " + userInfo.getUserId());
            context.getOptionalSingleton(TokenService.class).ifPresent(tokenService -> {
                System.err.println("Creating tokens for user: " + userInfo.getUserId());
                var tokens = tokenService.newTokens(userInfo.getUserId(), userInfo.getRoles(), userInfo.getClaims());
                var functions = context.environment.getIntegrationTestScript().getIntegrationTestFunctions();
                functions.getSetAccessToken().execute(tokens.getAccessToken());
                functions.getSetRenewToken().execute(tokens.getRenewToken());
            });
        }
    }


}
