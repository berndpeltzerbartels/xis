package one.xis.context.groovy

import one.xis.context.AppContext
import one.xis.context.Bean
import one.xis.context.Component
import one.xis.context.Init
import one.xis.context.Inject
import org.junit.jupiter.api.Test

import static org.assertj.core.api.Assertions.assertThat

class GroovyContextSupportTest {

    @Test
    void contextCreatesGroovyComponentsWithConstructorInjectionAndInvokesGroovyMethods() {
        def context = AppContext.builder()
                .withPackage('one.xis.context.groovy')
                .build()

        def consumer = context.getSingleton(GroovyConstructorConsumer)
        def fieldConsumer = context.getSingleton(GroovyFieldConsumer)
        def beanConsumer = context.getSingleton(GroovyBeanConsumer)

        assertThat(consumer.initialized).isTrue()
        assertThat(fieldConsumer.message()).isEqualTo('field service')
        assertThat(beanConsumer.message()).isEqualTo('service product')
    }
}

@Component
class GroovyService {
    String message() {
        'service'
    }
}

class GroovyProduct {
    private final String value

    GroovyProduct(String value) {
        this.value = value
    }

    String value() {
        value
    }
}

@Component
class GroovyConstructorConsumer {
    private final GroovyService service
    boolean initialized

    GroovyConstructorConsumer(GroovyService service) {
        this.service = service
    }

    @Init
    void initialize() {
        initialized = true
    }

    @Bean
    GroovyProduct product() {
        new GroovyProduct(service.message() + ' product')
    }

    String message() {
        product().value()
    }
}

@Component
class GroovyBeanConsumer {
    private final GroovyProduct product

    GroovyBeanConsumer(GroovyProduct product) {
        this.product = product
    }

    String message() {
        product.value()
    }
}

@Component
class GroovyFieldConsumer {
    @Inject
    private GroovyService service

    String message() {
        'field ' + service.message()
    }
}
