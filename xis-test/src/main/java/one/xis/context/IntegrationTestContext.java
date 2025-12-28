package one.xis.context;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.UserContextCreatedEvent;
import one.xis.UserContextImpl;
import one.xis.auth.UserInfo;
import one.xis.auth.UserInfoImpl;
import one.xis.auth.token.SecurityAttributes;
import one.xis.http.RequestContext;
import one.xis.http.RestControllerService;
import one.xis.server.PageUtil;
import one.xis.test.js.LocalStorage;
import one.xis.test.js.SessionStorage;

import java.util.*;
import java.util.stream.Collectors;

public class IntegrationTestContext {

    @Getter
    private final AppContext appContext;
    @Getter
    private final IntegrationTestEnvironment environment;
    private final UserInfo userInfo;
    private static final Object SYNC_LOCK = new Object();

    public static Builder builder() {
        return new Builder();
    }

    IntegrationTestContext(Collection<String> packages, UserInfo userInfo, Object... controllers) {
        this.appContext = internalContext(packages, controllers);
        this.environment = new IntegrationTestEnvironment(new BackendBridge(appContext.getSingleton(RestControllerService.class)));
        this.userInfo = userInfo;
    }

    public OpenPageResult openPage(String uri, Map<String, Object> parameters) {
        synchronized (SYNC_LOCK) {
            if (!parameters.isEmpty()) {
                uri += "?";
                uri += parameters.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
            }
            var userInfoFaker = appContext.getSingleton(UserContextFaker.class);
            userInfoFaker.setUserInfo(userInfo);
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

    public LocalStorage getLocalStorage() {
        return environment.getHtmlObjects().getLocalStorage();
    }

    public SessionStorage getSessionStorage() {
        return environment.getHtmlObjects().getSessionStorage();
    }

    public <T> T getSingleton(Class<T> type) {
        return appContext.getSingleton(type);
    }

    public <T> Optional<T> getOptionalSingleton(Class<T> type) {
        return appContext.getOptionalSingleton(type);
    }

    //@Override
    public Collection<Object> getSingletons() {
        return appContext.getSingletons();
    }

    //@Override
    public Collection<Object> getSingletons(Class<?> type) {
        return appContext.getSingletons().stream().filter(type::isInstance).toList();
    }

    private AppContext internalContext(Collection<String> packages, Object... controllers) {
        var builder = AppContextBuilder.createInstance()
                .withXIS()
                .withSingletonClass(TestUserInfoService.class)
                .withSingleton(new UserContextFaker())
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
        private String userPassword;

        public Builder withSingleton(Object o) {
            singletons.add(o);
            return this;
        }

        public Builder withMock(Object o) {
            return withSingleton(o);
        }

        public IntegrationTestContext build() {
            var context = new IntegrationTestContext(packages, userInfo, singletons.toArray());
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

        public Builder withLoggedInUser(UserInfo userInfo, String userPassword) {
            this.userInfo = userInfo;
            this.userPassword = userPassword;
            return this;
        }

        private static void bindUser(UserInfoImpl userInfo) {
            RequestContext.getInstance();
        }
    }

    @Setter
    static class UserContextFaker {
        private UserInfo userInfo;

        @XISEventListener
        public void onUserContextCreated(UserContextCreatedEvent event) {
            if (userInfo == null) {
                return;
            }
            ((UserContextImpl) event.getUserContext()).setSecurityAttributes(new TestSecurityAttributes(userInfo));

        }
    }

    @RequiredArgsConstructor
    static class TestSecurityAttributes implements SecurityAttributes {

        private final UserInfo userInfo;

        @Override
        public String getUserId() {
            return userInfo.getUserId();
        }

        @Override
        public Set<String> getRoles() {
            return userInfo.getRoles();
        }

        @Override
        public void setUserId(String userId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setRoles(Set<String> roles) {
            throw new UnsupportedOperationException();
        }
    }


}
