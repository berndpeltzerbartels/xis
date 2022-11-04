package one.xis.spring;

import one.xis.context.AppContext;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"one.xis.spring"})
@ServletComponentScan(basePackages = {"one.xis.spring"})
public class SpringContextAdapter {

    @Bean
    AppContext appContext() {
        return AppContext.getInstance("one.xis");
    }

}
