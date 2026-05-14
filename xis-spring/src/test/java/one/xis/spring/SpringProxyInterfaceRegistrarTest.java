package one.xis.spring;

import one.xis.spring.proxytest.TestDependency;
import one.xis.spring.proxytest.TestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class SpringProxyInterfaceRegistrarTest {

    @Test
    void registersInterfacesAnnotatedWithProxyAnnotationsAsSpringBeans() {
        try (var context = new AnnotationConfigApplicationContext()) {
            AutoConfigurationPackages.register(context.getDefaultListableBeanFactory(), "one.xis.spring.proxytest");
            context.register(SpringProxyInterfaceRegistrar.class, TestConfiguration.class);

            context.refresh();

            assertThat(context.getBean(TestRepository.class).value()).isEqualTo("spring dependency");
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {

        @Bean
        TestDependency testDependency() {
            return new TestDependency("spring dependency");
        }
    }
}
