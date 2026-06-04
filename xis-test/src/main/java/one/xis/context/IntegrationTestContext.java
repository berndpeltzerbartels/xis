package one.xis.context;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.UserContextCreatedEvent;
import one.xis.UserContextImpl;
import one.xis.auth.LocalCredentialService;
import one.xis.auth.UserAccount;
import one.xis.auth.UserAccountImpl;
import one.xis.auth.UserAccountService;
import one.xis.auth.token.SecurityAttributes;
import one.xis.http.RequestContext;
import one.xis.http.RestControllerService;
import one.xis.server.PageUtil;
import one.xis.test.js.LocalStorage;
import one.xis.test.js.SessionStorage;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public class IntegrationTestContext {

    @Getter
    private final AppContext appContext;
    private final UserAccount userAccount;
    private final String userPassword;
    private final BackendBridge backendBridge;
    private final TestClient primaryClient;
    private final List<TestClient> clients = new ArrayList<>();
    private boolean primaryClientOpened;

    private static final Object SYNC_LOCK = new Object();

    public static Builder builder() {
        return new Builder();
    }

    IntegrationTestContext(Collection<String> packages, UserAccount userAccount, String userPassword, Collection<Object> singletons,
                           Collection<Class<? extends Annotation>> componentAnnotations,
                           Collection<Class<? extends Annotation>> beanMethodAnnotations,
                           Collection<Class<? extends Annotation>> beanInitAnnotations,
                           Collection<Class<? extends Annotation>> dependencyFieldAnnotations) {
        this.appContext = internalContext(packages, singletons, componentAnnotations, beanMethodAnnotations,
                beanInitAnnotations, dependencyFieldAnnotations);
        this.userAccount = userAccount;
        this.userPassword = userPassword;
        storeLoggedInUserCredentials();
        this.backendBridge = new BackendBridge(appContext.getSingleton(RestControllerService.class));
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
        var environment = new IntegrationTestEnvironment(backendBridge);
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
            var userAccountFaker = appContext.getSingleton(UserContextFaker.class);
            userAccountFaker.setUserAccount(userAccount);
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

    public JavascriptResponse invokeBackend(String httpMethod, String uri) {
        return invokeBackend(httpMethod, uri, Collections.emptyMap(), null);
    }

    public JavascriptResponse invokeBackend(String httpMethod, String uri, Map<String, String> headers) {
        return invokeBackend(httpMethod, uri, headers, null);
    }

    public JavascriptResponse invokeBackend(String httpMethod, String uri, Map<String, String> headers, String body) {
        synchronized (SYNC_LOCK) {
            var userAccountFaker = appContext.getSingleton(UserContextFaker.class);
            userAccountFaker.setUserAccount(userAccount);
            return backendBridge.invokeBackend(httpMethod, uri, headers, body);
        }
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

    private AppContext internalContext(Collection<String> packages, Collection<Object> singletons,
                                       Collection<Class<? extends Annotation>> componentAnnotations,
                                       Collection<Class<? extends Annotation>> beanMethodAnnotations,
                                       Collection<Class<? extends Annotation>> beanInitAnnotations,
                                       Collection<Class<? extends Annotation>> dependencyFieldAnnotations) {
        var builder = AppContextBuilder.createInstance()
                .withXIS()
                .withSingleton(new UserContextFaker());
        componentAnnotations.forEach(builder::withComponentAnnotation);
        beanMethodAnnotations.forEach(builder::withBeanMethodAnnotation);
        beanInitAnnotations.forEach(builder::withBeanInitAnnotation);
        dependencyFieldAnnotations.forEach(builder::withDependencyFieldAnnotation);
        if (!containsUserAccountService(singletons)) {
            builder.withSingletonClass(TestUserAccountService.class);
        }
        if (!containsLocalCredentialService(singletons)) {
            builder.withSingletonClass(TestLocalCredentialService.class);
        }
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

    private boolean containsUserAccountService(Collection<Object> singletons) {
        return singletons.stream().anyMatch(singleton -> {
            if (singleton instanceof Class<?> clazz) {
                return UserAccountService.class.isAssignableFrom(clazz);
            }
            return singleton instanceof UserAccountService<?>;
        });
    }

    private boolean containsLocalCredentialService(Collection<Object> singletons) {
        return singletons.stream().anyMatch(singleton -> {
            if (singleton instanceof Class<?> clazz) {
                return LocalCredentialService.class.isAssignableFrom(clazz);
            }
            return singleton instanceof LocalCredentialService;
        });
    }

    private void storeLoggedInUserCredentials() {
        if (userAccount == null || userPassword == null) {
            return;
        }
        appContext.getOptionalSingleton(UserAccountService.class)
                .ifPresent(service -> ((UserAccountService<UserAccount>) service).saveUserAccount(userAccount));
        appContext.getOptionalSingleton(LocalCredentialService.class)
                .ifPresent(service -> service.setPassword(userAccount.getUserId(), userPassword));
    }

    @SuppressWarnings("unused")
    public static class Builder {

        private final Collection<Object> singletons = new HashSet<>();
        private final Collection<String> packages = new HashSet<>();
        private final Collection<String> ignorePackages = new HashSet<>();
        private final Collection<Class<? extends Annotation>> componentAnnotations = new HashSet<>();
        private final Collection<Class<? extends Annotation>> beanMethodAnnotations = new HashSet<>();
        private final Collection<Class<? extends Annotation>> beanInitAnnotations = new HashSet<>();
        private final Collection<Class<? extends Annotation>> dependencyFieldAnnotations = new HashSet<>();
        private UserAccount userAccount;
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
            return new IntegrationTestContext(packages, userAccount, userPassword, singletons, componentAnnotations,
                    beanMethodAnnotations, beanInitAnnotations, dependencyFieldAnnotations);
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

        /**
         * Adds an annotation that marks scanned classes as XIS context components.
         */
        public Builder withComponentAnnotation(Class<? extends Annotation> annotation) {
            componentAnnotations.add(annotation);
            return this;
        }

        /**
         * Adds an annotation that marks methods as bean factory methods.
         */
        public Builder withBeanMethodAnnotation(Class<? extends Annotation> annotation) {
            beanMethodAnnotations.add(annotation);
            return this;
        }

        /**
         * Adds an annotation that marks methods as initialization callbacks.
         */
        public Builder withBeanInitAnnotation(Class<? extends Annotation> annotation) {
            beanInitAnnotations.add(annotation);
            return this;
        }

        /**
         * Adds an annotation that marks fields as dependency injection points.
         */
        public Builder withDependencyFieldAnnotation(Class<? extends Annotation> annotation) {
            dependencyFieldAnnotations.add(annotation);
            return this;
        }

        public Builder withLoggedInUser(UserAccount userAccount, String userPassword) {
            this.userAccount = userAccount;
            this.userPassword = userPassword;
            return this;
        }

        private static void bindUser(UserAccountImpl userAccount) {
            RequestContext.getInstance();
        }
    }

    @Setter
    static class UserContextFaker {
        private UserAccount userAccount;

        @EventListener
        public void onUserContextCreated(UserContextCreatedEvent event) {
            if (userAccount == null) {
                return;
            }
            ((UserContextImpl) event.getUserContext()).setSecurityAttributes(new TestSecurityAttributes(userAccount));

        }
    }

    @RequiredArgsConstructor
    static class TestSecurityAttributes implements SecurityAttributes {

        private final UserAccount userAccount;

        @Override
        public String getUserId() {
            return userAccount.getUserId();
        }

        @Override
        public Set<String> getRoles() {
            return userAccount.getRoles();
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
