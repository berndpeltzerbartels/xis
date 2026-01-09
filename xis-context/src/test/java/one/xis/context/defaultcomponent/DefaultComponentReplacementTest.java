package one.xis.context.defaultcomponent;

import one.xis.context.AppContext;
import one.xis.context.defaultcomponent.beanmethod.Config;
import one.xis.context.defaultcomponent.fallback.DefaultProcessor;
import one.xis.context.defaultcomponent.fallback.FallbackCollectionConsumer;
import one.xis.context.defaultcomponent.fallback.FallbackConsumer;
import one.xis.context.defaultcomponent.multiple.CustomHandlerA;
import one.xis.context.defaultcomponent.multiple.CustomHandlerB;
import one.xis.context.defaultcomponent.multiple.DefaultHandler;
import one.xis.context.defaultcomponent.multiple.MultiConsumer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultComponentReplacementTest {

    @Test
    void shouldReplaceDefaultComponentWithCustomImplementationForSingleValue() {
        var context = AppContext.builder()
                .withBasePackageClass(Service.class)
                .build();

        var consumer = context.getSingleton(ServiceConsumer.class);
        assertThat(consumer).isNotNull();
        assertThat(consumer.getService()).isInstanceOf(CustomService.class);
        assertThat(consumer.getService()).isNotInstanceOf(DefaultServiceImpl.class);
    }

    @Test
    void shouldReplaceDefaultComponentWithCustomImplementationInCollection() {
        var context = AppContext.builder()
                .withBasePackageClass(Service.class)
                .build();

        var consumer = context.getSingleton(CollectionConsumer.class);
        assertThat(consumer).isNotNull();
        assertThat(consumer.getServices()).hasSize(1);
        assertThat(consumer.getServices()).hasOnlyElementsOfType(CustomService.class);
        assertThat(consumer.getServices()).doesNotHaveAnyElementsOfTypes(DefaultServiceImpl.class);
    }

    @Test
    void shouldUseDefaultComponentWhenNoCustomImplementationExists() {
        var context = AppContext.builder()
                .withPackage("one.xis.context.defaultcomponent.fallback")
                .build();

        var consumer = context.getSingleton(FallbackConsumer.class);
        assertThat(consumer).isNotNull();
        assertThat(consumer.getProcessor()).isInstanceOf(DefaultProcessor.class);
    }

    @Test
    void shouldUseDefaultComponentInCollectionWhenNoCustomImplementationExists() {
        var context = AppContext.builder()
                .withPackage("one.xis.context.defaultcomponent.fallback")
                .build();

        var consumer = context.getSingleton(FallbackCollectionConsumer.class);
        assertThat(consumer).isNotNull();
        assertThat(consumer.getProcessors()).hasSize(1);
        assertThat(consumer.getProcessors()).hasOnlyElementsOfType(DefaultProcessor.class);
    }

    @Test
    void shouldNotCreateBeanMethodsFromDefaultComponentWhenCustomExists() {
        var context = AppContext.builder()
                .withPackage("one.xis.context.defaultcomponent.beanmethod")
                .build();

        // DefaultConfigProvider hat eine @Bean Methode die Config erstellt
        // Diese sollte nicht ausgeführt werden, weil CustomConfigProvider existiert
        var configs = context.getSingletons(Config.class);
        assertThat(configs).hasSize(1);

        var config = context.getSingleton(Config.class);
        assertThat(config.getSource()).isEqualTo("custom");
    }

    @Test
    void shouldReplaceMultipleDefaultComponentsWithMultipleCustomImplementations() {
        var context = AppContext.builder()
                .withPackage("one.xis.context.defaultcomponent.multiple")
                .build();

        var consumer = context.getSingleton(MultiConsumer.class);
        assertThat(consumer).isNotNull();
        assertThat(consumer.getHandlers()).hasSize(2);
        assertThat(consumer.getHandlers()).hasOnlyElementsOfTypes(CustomHandlerA.class, CustomHandlerB.class);
        assertThat(consumer.getHandlers()).doesNotHaveAnyElementsOfTypes(DefaultHandler.class);
    }
}
