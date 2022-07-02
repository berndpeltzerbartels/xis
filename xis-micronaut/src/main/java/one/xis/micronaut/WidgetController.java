package one.xis.micronaut;

import io.micronaut.context.BeanContext;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.xis.Widget;
import one.xis.widget.Widgets;

@Controller
public class WidgetController {

    private Widgets widgets;

    @Inject
    private BeanContext beanContext;

    @Inject
    private XISContextHolder xisContextHolder;

    @PostConstruct
    void init() {
        widgets = xisContextHolder.getAppContext().getSingleton(Widgets.class);

        beanContext.getBeanDefinitions(Qualifiers.byStereotype(Widget.class)).stream()//
                .map(BeanDefinition::getBeanType)
                .map(beanContext::getBean)
                .forEach(bean -> widgets.addWidget(bean.getClass().getName(), bean));
    }

    @Get(produces = "text/javascript", uri = "/xis/widget/{widgetUrn}")
    String getWidget(@PathVariable("widgetUrn") String widgetUrn) {
        return widgets.getWidget(widgetUrn).getJavascript();
    }

}