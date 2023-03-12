package one.xis.spring;

import one.xis.context.AppContext;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration
@ComponentScan(basePackages = {"one.xis.spring"})
@ServletComponentScan(basePackages = {"one.xis.spring"})
public class SpringContextAdapter implements BeanPostProcessor {

    private Collection<Object> pageControllers;

    @Bean
    AppContext appContext() {
        return AppContext.getInstance("one.xis");
    }

}
