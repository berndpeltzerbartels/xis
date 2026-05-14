package one.xis.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class SpringProxyConfiguration {

    @Bean
    static SpringProxyInterfaceRegistrar springProxyInterfaceRegistrar() {
        return new SpringProxyInterfaceRegistrar();
    }
}
