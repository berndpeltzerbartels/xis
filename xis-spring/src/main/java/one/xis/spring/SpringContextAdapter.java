package one.xis.spring;

import lombok.RequiredArgsConstructor;
import one.xis.Page;
import one.xis.Widget;
import one.xis.context.AppContextBuilder;
import one.xis.server.FrontendService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.Collection;
import java.util.HashSet;

@Configuration
@ComponentScan(basePackages = {"one.xis.spring"})
@ServletComponentScan(basePackages = {"one.xis.spring"})
@RequiredArgsConstructor
class SpringContextAdapter implements BeanPostProcessor {

    private final SpringHtmlFilter filter;
    private final SpringController controller;

    private final Collection<Object> controllers = new HashSet<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(Page.class) || bean.getClass().isAnnotationPresent(Widget.class)) {
            controllers.add(bean);
        }
        return bean;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        var context = AppContextBuilder.createInstance()
                .withSingletons(controllers)
                .withPackage("one.xis")
                .build();
        var frontendService = context.getSingleton(FrontendService.class);
        filter.setFrontendService(frontendService);
        controller.setFrontendService(frontendService);
    }
}
