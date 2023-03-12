package one.xis.spring;

import one.xis.Page;
import one.xis.Widget;
import one.xis.context.AppContextBuilder;
import one.xis.server.FrontendService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.HashSet;

@Configuration
@ComponentScan(basePackages = {"one.xis.spring"})
@ServletComponentScan(basePackages = {"one.xis.spring"})
class SpringContextAdapter implements BeanPostProcessor {
    

    private final Collection<Object> controllers = new HashSet<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(Page.class) || bean.getClass().isAnnotationPresent(Widget.class)) {
            controllers.add(bean);
        }
        return bean;
    }

    @Bean
    FrontendService frontendService() {
        var context = AppContextBuilder.createInstance()
                .withSingeltons(controllers)
                .withPackage("one.xis")
                .build();
        return context.getSingleton(FrontendService.class);
    }

}
