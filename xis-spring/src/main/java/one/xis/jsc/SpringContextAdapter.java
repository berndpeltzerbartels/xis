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
    private PageJavascripts pageJavascripts;

    @PostConstruct
    void init() {
        pageJavascripts = pages();
        widgets = widgets();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(Widget.class)) {
            widgets.add(bean);
        }
        if (bean.getClass().isAnnotationPresent(Page.class)) {
            pageJavascripts.add(bean);
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
    PageJavascripts pages() {
        return appContext().getSingleton(PageJavascripts.class);
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
