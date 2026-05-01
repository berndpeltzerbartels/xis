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
    private final UserInfo userInfo;
    private final TestClient primaryClient;
    private final List<TestClient> clients = new ArrayList<>();
    private boolean primaryClientOpened;

    private static final Object SYNC_LOCK = new Object();

    public static Builder builder() {
        return new Builder();
    }

    IntegrationTestContext(Collection<String> packages, UserInfo userInfo, Collection<Object> singletons) {
        this.appContext = internalContext(packages, singletons);
        this.userInfo = userInfo;
        this.primaryClient = createClient();
    }

    /**
     * After the JS context is ready, wires all {@link PushEventSimulatorAware} singletons
     * with a transport-neutral push-event simulator backed by the active JS test runtime.
     * Called by the Builder after reset().
     */
    void wirePushEventSimulator() {
        clients.forEach(this::wirePushEventSimulator);
    }

    private void wirePushEventSimulator(TestClient client) {
        PushEventSimulator simulator = updateEventKey -> client.getTestEnvironment().getIntegrationTestScript()
                .getIntegrationTestFunctions()
                .getSimulatePushEvent()
                .execute(updateEventKey);
        appContext.getSingletons().stream()
                .filter(s -> s instanceof PushEventSimulatorAware)
                .map(s -> (PushEventSimulatorAware) s)
                .forEach(aware -> aware.setPushEventSimulator(simulator));
    }

    private TestClient createClient() {
        var environment = new IntegrationTestEnvironment(new BackendBridge(appContext.getSingleton(RestControllerService.class)));
        environment.getIntegrationTestScript().reset();
        var client = new TestClient(appContext, environment);
        clients.add(client);
        wirePushEventSimulator(client);
        return client;
    }

    private TestClient nextClient() {
        if (!primaryClientOpened) {
            primaryClientOpened = true;
            return primaryClient;
        }
        return createClient();
    }

    public TestClient openPage(String uri, Map<String, Object> parameters) {
        synchronized (SYNC_LOCK) {
            if (!parameters.isEmpty()) {
                uri += "?";
                uri += parameters.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
            }
            var userInfoFaker = appContext.getSingleton(UserContextFaker.class);
            userInfoFaker.setUserInfo(userInfo);
            var client = nextClient();
            client.getTestEnvironment().openPage(uri);
            return client;
        }
    }

    public TestClient openPage(String uri) {
        return openPage(uri, Collections.emptyMap());
    }


    public TestClient openPage(Class<?> pageController) {
        return openPage(PageUtil.getUrl(pageController), Collections.emptyMap());
    }

    /**
     * Simulates a refresh event for all open test clients.
     *
     * <p>Returns the most recently opened client so the test can immediately inspect the
     * updated DOM state after the refresh event has been processed.
     *
     * @param updateEventKey the event key, e.g. "gameUpdated"
     * @return the updated DOM state
     */
    public TestClient simulatePushEvent(String updateEventKey) {
        synchronized (SYNC_LOCK) {
            if (clients.isEmpty()) {
                throw new IllegalStateException("No test client available. Call openPage(...) before simulating refresh events.");
            }
            clients.forEach(client -> client.getTestEnvironment()
                    .getIntegrationTestScript()
                    .getIntegrationTestFunctions()
                    .getSimulatePushEvent()
                    .execute(updateEventKey));
            return clients.get(clients.size() - 1);
        }
    }

    public LocalStorage getLocalStorage() {
        return primaryClient.getLocalStorage();
    }

    public SessionStorage getSessionStorage() {
        return primaryClient.getSessionStorage();
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

    private AppContext internalContext(Collection<String> packages, Collection<Object> singletons) {
        var builder = AppContextBuilder.createInstance()
                .withXIS()
                .withSingletonClass(TestUserInfoService.class)
                .withSingleton(new UserContextFaker());
        for (var s : singletons) {
            if (s instanceof Class<?> clazz) {
                builder.withSingletonClass(clazz);
            } else {
                builder.withSingleton(s);
            }
        }
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

        /**
         * Registers a class as a singleton. The class is instantiated by the DI container,
         * so constructor injection, {@code @Inject} fields and {@code @Init} methods all work.
         */
        public Builder withSingleton(Class<?> clazz) {
            singletons.add(clazz);
            return this;
        }

        public Builder withMock(Object o) {
            return withSingleton(o);
        }

        public IntegrationTestContext build() {
            return new IntegrationTestContext(packages, userInfo, singletons);
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

        @EventListener
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
