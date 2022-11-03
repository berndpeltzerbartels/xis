package one.xis.spring.widget;

import one.xis.Widget;
import one.xis.context.AppContext;
import one.xis.widget.WidgetService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
class WidgetConfig implements BeanPostProcessor {

    @Autowired
    private AppContext appContext;
    private WidgetService widgetService;

    @PostConstruct
    void init() {
        widgetService = appContext.getSingleton(WidgetService.class);
    }

    @Bean
    WidgetService widgetService() {
        return widgetService;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(Widget.class)) {
            widgetService.addWidgetConroller(bean);
        }
        return bean;
    }

}
