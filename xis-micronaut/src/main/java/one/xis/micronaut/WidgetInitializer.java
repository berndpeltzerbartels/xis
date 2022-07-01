package one.xis.micronaut;


import io.micronaut.context.BeanContext;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import one.xis.Widget;
import one.xis.context.AppContext;
import one.xis.widget.WidgetUtil;
import one.xis.widget.Widgets;

import javax.annotation.PostConstruct;

@Singleton
class WidgetInitializer {
    
    @Inject
    private BeanContext beanContext;

    @PostConstruct
    void init() {
        AppContext appContext = AppContext.getInstance("one.xis");
        Widgets widgets = appContext.getSingleton(Widgets.class);

        beanContext.getBeanDefinitions(Qualifiers.byStereotype(Widget.class)).stream()//
                .map(BeanDefinition::getBeanType)
                .map(beanContext::getBean)
                .forEach(bean -> widgets.addWidget(getWidgetId(bean), bean));
    }

    private String getWidgetId(Object widgetController) {
        return WidgetUtil.getWidgetId(widgetController);
    }
}