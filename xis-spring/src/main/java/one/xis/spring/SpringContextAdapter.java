package one.xis.spring;

import one.xis.Page;
import one.xis.Widget;
import one.xis.context.AppContext;
import one.xis.widget.Widgets;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
class SpringContextAdapter implements BeanPostProcessor {

    private Widgets widgets;

    @PostConstruct
    void init() {
        AppContext appContext = getAppContext();
        widgets = appContext.getSingleton(Widgets.class);
    }

    private AppContext getAppContext() {
        return AppContext.getInstance("one.xis");
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(Widget.class)) {
            widgets.addWidget(bean.getClass().getName(), bean);
        }
        if (bean.getClass().isAnnotationPresent(Page.class)) {
            //widgets.addWidget(getWidgetId(bean), bean);
        }
        return bean;
    }
}
