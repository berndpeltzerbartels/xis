package one.xis.context;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static org.junit.jupiter.api.Assertions.*;

class AopProxyTest {

    @Test
    void injectsAopProxyForInterfaceConsumer() {
        CountingAdvice.invocations = 0;

        AppContext context = AppContext.builder()
                .withSingletonClasses(Consumer.class, GreetingServiceImpl.class)
                .build();
        Consumer consumer = context.getSingleton(Consumer.class);

        String greeting = consumer.greetingService.greet("Bernd");

        assertEquals("[Hello Bernd]", greeting);
        assertEquals(1, CountingAdvice.invocations);
        assertTrue(consumer.greetingService instanceof XisProxy);
        assertInstanceOf(GreetingServiceImpl.class, ((XisProxy) consumer.greetingService).xisTarget());
    }

    @Test
    void createsAopProxyForBeanMethodReturnValue() {
        CountingAdvice.invocations = 0;

        AppContext context = AppContext.builder()
                .withSingletonClasses(BeanConsumer.class, BeanConfiguration.class)
                .build();
        BeanConsumer consumer = context.getSingleton(BeanConsumer.class);

        String greeting = consumer.greetingService.greet("XIS");

        assertEquals("[Hello XIS]", greeting);
        assertEquals(1, CountingAdvice.invocations);
        assertTrue(consumer.greetingService instanceof XisProxy);
    }

    @Test
    void proxiedClassKeepsConstructorInjectionOnTarget() {
        CountingAdvice.invocations = 0;

        AppContext context = AppContext.builder()
                .withSingletonClasses(Consumer.class, GreetingServiceWithConstructorInjection.class, GreetingPrefix.class)
                .build();
        Consumer consumer = context.getSingleton(Consumer.class);

        String greeting = consumer.greetingService.greet("Constructor");

        assertEquals("[Hi Constructor]", greeting);
        assertEquals(1, CountingAdvice.invocations);
        assertTrue(consumer.greetingService instanceof XisProxy);
    }

    @Test
    void aopProxyCanBeConstructorDependency() {
        CountingAdvice.invocations = 0;

        AppContext context = AppContext.builder()
                .withSingletonClasses(ConstructorConsumer.class, GreetingServiceImpl.class)
                .build();
        ConstructorConsumer consumer = context.getSingleton(ConstructorConsumer.class);

        String greeting = consumer.greet("Dependency");

        assertEquals("[Hello Dependency]", greeting);
        assertEquals(1, CountingAdvice.invocations);
        assertTrue(consumer.greetingService instanceof XisProxy);
    }

    @Test
    void classLevelAdviceAppliesToInterfaceMethods() {
        CountingAdvice.invocations = 0;

        AppContext context = AppContext.builder()
                .withSingletonClasses(ClassLevelConsumer.class, ClassLevelGreetingServiceImpl.class)
                .build();
        ClassLevelConsumer consumer = context.getSingleton(ClassLevelConsumer.class);

        assertEquals("[Hello Class]", consumer.greetingService.greet("Class"));
        assertEquals("[Bye Class]", consumer.greetingService.farewell("Class"));
        assertEquals(2, CountingAdvice.invocations);
    }

    @Test
    void failsWhenAdviceMethodHasNoInterface() {
        AppContextException exception = assertThrows(AppContextException.class, () -> AppContext.builder()
                .withSingletonClass(AdvisedClassWithoutInterface.class)
                .build());

        assertTrue(exception.getMessage().contains("needs at least one non-framework interface"));
    }

    @Test
    void failsWhenAdvisedMethodIsNotDeclaredOnInterface() {
        AppContextException exception = assertThrows(AppContextException.class, () -> AppContext.builder()
                .withSingletonClass(PartiallyAdvisedServiceImpl.class)
                .build());

        assertTrue(exception.getMessage().contains("is not declared by any non-framework interface"));
    }

    @Test
    void failsWhenClassLevelAdviceHasNoInterface() {
        AppContextException exception = assertThrows(AppContextException.class, () -> AppContext.builder()
                .withSingletonClass(ClassLevelAdviceWithoutInterface.class)
                .build());

        assertTrue(exception.getMessage().contains("needs at least one non-framework interface"));
    }

    static class Consumer {
        @Inject
        GreetingService greetingService;
    }

    static class ConstructorConsumer {
        private final GreetingService greetingService;

        ConstructorConsumer(GreetingService greetingService) {
            this.greetingService = greetingService;
        }

        String greet(String name) {
            return greetingService.greet(name);
        }
    }

    static class BeanConsumer {
        @Inject
        GreetingService greetingService;
    }

    static class BeanConfiguration {
        @Bean
        GreetingService greetingService() {
            return new GreetingServiceImpl();
        }
    }

    interface GreetingService {
        String greet(String name);
    }

    static class GreetingServiceImpl implements GreetingService {
        @Counted
        public String greet(String name) {
            return "Hello " + name;
        }
    }

    static class GreetingServiceWithConstructorInjection implements GreetingService {
        private final GreetingPrefix prefix;

        GreetingServiceWithConstructorInjection(GreetingPrefix prefix) {
            this.prefix = prefix;
        }

        @Counted
        public String greet(String name) {
            return prefix.value + " " + name;
        }
    }

    static class GreetingPrefix {
        private final String value = "Hi";
    }

    static class ClassLevelConsumer {
        @Inject
        ClassLevelGreetingService greetingService;
    }

    interface ClassLevelGreetingService {
        String greet(String name);

        String farewell(String name);
    }

    @Counted
    static class ClassLevelGreetingServiceImpl implements ClassLevelGreetingService {

        @Override
        public String greet(String name) {
            return "Hello " + name;
        }

        @Override
        public String farewell(String name) {
            return "Bye " + name;
        }
    }

    static class AdvisedClassWithoutInterface {
        @Counted
        public String run() {
            return "ignored";
        }
    }

    interface PartialService {
        String visible();
    }

    static class PartiallyAdvisedServiceImpl implements PartialService {

        @Override
        public String visible() {
            return "visible";
        }

        @Counted
        public String hidden() {
            return "hidden";
        }
    }

    @Counted
    static class ClassLevelAdviceWithoutInterface {
        public String run() {
            return "ignored";
        }
    }

    @UseAdvice(CountingAdvice.class)
    @Target({TYPE, METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Counted {
    }

    public static class CountingAdvice implements Advice {
        static int invocations;

        @Override
        public Object around(AdviceInvocation invocation) throws Throwable {
            invocations++;
            return "[" + invocation.proceed() + "]";
        }
    }
}
