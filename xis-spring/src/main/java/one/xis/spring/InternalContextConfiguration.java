package one.xis.spring;

import one.xis.context.AppContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class InternalContextConfiguration {

    @Bean
    AppContext appContext() {
        return AppContext.getInstance("one.xis");
    }
}
