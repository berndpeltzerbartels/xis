package one.xis.jsc;

import one.xis.Page;
import one.xis.Widget;
import one.xis.context.AppContext;
import one.xis.resource.ResourceFiles;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SpringContextAdapter implements BeanPostProcessor {

    private Widgets widgets;
    private Pages pages;

    @PostConstruct
    void init() {
        pages = pages();
        widgets = widgets();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(Widget.class)) {
            widgets.add(beanName, bean);
        }
        if (bean.getClass().isAnnotationPresent(Page.class)) {
            pages.add(beanName, bean);
        }
        return bean;
    }

    @Bean
    AppContext appContext() {
        return AppContext.getInstance("one.xis");
    }

    @Bean
    Widgets widgets() {
        return appContext().getSingleton(Widgets.class);
    }

    @Bean
    Pages pages() {
        return appContext().getSingleton(Pages.class);
    }

    @Bean
    ResourceFiles resourceFiles() {
        return appContext().getSingleton(ResourceFiles.class);
    }

    @Bean
    RootPage rootPage() {
        return appContext().getSingleton(RootPage.class);
    }

    @Bean
    InitializerScript initializer() {
        return appContext().getSingleton(InitializerScript.class);
    }

}
