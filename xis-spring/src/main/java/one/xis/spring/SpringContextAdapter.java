package one.xis.spring;

import lombok.RequiredArgsConstructor;
import one.xis.Page;
import one.xis.Push;
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

    private final SpringFilter springFilter;
    private final SpringController springController;

    private final Collection<Object> controllers = new HashSet<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (isBeanClass(bean.getClass())) {
            controllers.add(bean);
        }
        return bean;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        var context = AppContextBuilder.createInstance()
                .withSingletons(controllers)
                .withXIS()
                .build();
        var frontendService = context.getSingleton(FrontendService.class);
        springFilter.setFrontendService(frontendService);
        springController.setFrontendService(frontendService);
    }

    private boolean isBeanClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Page.class)
                || clazz.isAnnotationPresent(Widget.class)
                || clazz.isAnnotationPresent(Push.class);
    }
}
