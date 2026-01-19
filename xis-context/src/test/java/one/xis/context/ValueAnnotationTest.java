package one.xis.context;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueAnnotationTest {

    @Test
    void injectStringValue() {
        var appContext = new TestContextBuilder()
                .withSingletonClasses(StringValueComponent.class)
                .withComponentAnnotation(Component.class)
                .build();

        StringValueComponent component = appContext.getSingleton(StringValueComponent.class);
        
        assertThat(component.stringValue).isEqualTo("Hello World");
        assertThat(component.appName).isEqualTo("TestApp");
    }

    @Test
    void injectPrimitiveValues() {
        var appContext = new TestContextBuilder()
                .withSingletonClasses(PrimitiveValueComponent.class)
                .withComponentAnnotation(Component.class)
                .build();

        PrimitiveValueComponent component = appContext.getSingleton(PrimitiveValueComponent.class);
        
        assertThat(component.intValue).isEqualTo(42);
        assertThat(component.longValue).isEqualTo(123456789L);
        assertThat(component.booleanValue).isTrue();
        assertThat(component.doubleValue).isEqualTo(3.14);
    }

    @Test
    void injectWrapperValues() {
        var appContext = new TestContextBuilder()
                .withSingletonClasses(WrapperValueComponent.class)
                .withComponentAnnotation(Component.class)
                .build();

        WrapperValueComponent component = appContext.getSingleton(WrapperValueComponent.class);
        
        assertThat(component.integerValue).isEqualTo(42);
        assertThat(component.longValue).isEqualTo(123456789L);
        assertThat(component.booleanValue).isTrue();
        assertThat(component.doubleValue).isEqualTo(3.14);
    }

    @Test
    void supportsBothSyntaxVariants() {
        var appContext = new TestContextBuilder()
                .withSingletonClasses(SyntaxVariantsComponent.class)
                .withComponentAnnotation(Component.class)
                .build();

        SyntaxVariantsComponent component = appContext.getSingleton(SyntaxVariantsComponent.class);
        
        assertThat(component.withoutBraces).isEqualTo("Hello World");
        assertThat(component.withBraces).isEqualTo("Hello World");
    }

    @Test
    void valueInjectedBeforeDependencies() {
        var appContext = new TestContextBuilder()
                .withSingletonClasses(ValueBeforeDependencyComponent.class, DependencyComponent.class)
                .withComponentAnnotation(Component.class)
                .build();

        ValueBeforeDependencyComponent component = appContext.getSingleton(ValueBeforeDependencyComponent.class);
        
        assertThat(component.valueAvailableInInit).isTrue();
        assertThat(component.configValue).isEqualTo("TestApp");
    }

    @Test
    void throwsExceptionForMissingProperty() {
        assertThatThrownBy(() -> 
            new TestContextBuilder()
                .withSingletonClasses(MissingPropertyComponent.class)
                .withComponentAnnotation(Component.class)
                .build()
        ).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Property 'non.existent.property' not found");
    }

    @Test
    void injectValueWhenBeanPassedAsObject() {
        // This tests the case where a ready bean instance is passed directly
        StringValueComponent bean = new StringValueComponent();
        
        var appContext = new TestContextBuilder()
                .withSingleton(bean)
                .withComponentAnnotation(Component.class)
                .build();

        StringValueComponent component = appContext.getSingleton(StringValueComponent.class);
        
        assertThat(component).isSameAs(bean);
        assertThat(component.stringValue).isEqualTo("Hello World");
        assertThat(component.appName).isEqualTo("TestApp");
    }

    @Test
    void injectValueBeforeDependenciesWhenBeanPassedAsObject() {
        // This tests that @Value injection happens before dependencies even when bean is passed as object
        ValueBeforeDependencyComponent bean = new ValueBeforeDependencyComponent();
        DependencyComponent dependency = new DependencyComponent();
        
        var appContext = new TestContextBuilder()
                .withSingleton(bean)
                .withSingleton(dependency)
                .withComponentAnnotation(Component.class)
                .build();

        ValueBeforeDependencyComponent component = appContext.getSingleton(ValueBeforeDependencyComponent.class);
        
        assertThat(component).isSameAs(bean);
        assertThat(component.valueAvailableInInit).isTrue();
        assertThat(component.configValue).isEqualTo("TestApp");
    }

    @Component
    static class StringValueComponent {
        @Value("test.string")
        String stringValue;
        
        @Value("app.name")
        String appName;
    }

    @Component
    static class PrimitiveValueComponent {
        @Value("test.int")
        int intValue;
        
        @Value("test.long")
        long longValue;
        
        @Value("test.boolean")
        boolean booleanValue;
        
        @Value("test.double")
        double doubleValue;
    }

    @Component
    static class WrapperValueComponent {
        @Value("test.int")
        Integer integerValue;
        
        @Value("test.long")
        Long longValue;
        
        @Value("test.boolean")
        Boolean booleanValue;
        
        @Value("test.double")
        Double doubleValue;
    }

    @Component
    static class SyntaxVariantsComponent {
        @Value("test.string")
        String withoutBraces;
        
        @Value("${test.string}")
        String withBraces;
    }

    @Component
    static class ValueBeforeDependencyComponent {
        @Value("app.name")
        String configValue;
        
        @Inject
        DependencyComponent dependency;
        
        boolean valueAvailableInInit = false;
        
        @Init
        void initialize() {
            // @Value should be injected before @Init is called
            valueAvailableInInit = (configValue != null && !configValue.isEmpty());
        }
    }

    @Component
    static class DependencyComponent {
    }

    @Component
    static class MissingPropertyComponent {
        @Value("non.existent.property")
        String missingValue;
    }
}
