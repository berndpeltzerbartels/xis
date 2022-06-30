package one.xis.spring;

import one.xis.Widget;
import one.xis.context.AppContext;
import one.xis.widget.Widgets;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;

@Component
class SpringContextAdapter {

    @Widget
    private Collection<Object> widgetControllers;

    @PostConstruct
    void init() {
        AppContext appContext = getXISAppContext();
        initWidgets(appContext);
    }

    private void initWidgets(AppContext appContext) {
        Widgets widgets = getWidgets(appContext);
        registerWidgets(widgets);
    }

    private void registerWidgets(Widgets widgets) {
        widgetControllers.forEach(controller -> widgets.addWidget(getWidgetId(controller), controller));
    }

    private Widgets getWidgets(AppContext appContext) {
        return appContext.getSingleton(Widgets.class);
    }

    private AppContext getXISAppContext() {
        return AppContext.getInstance("one.xis");
    }

    private String getWidgetId(Object widgetController) {
        String annoValue = widgetController.getClass().getAnnotation(Widget.class).value();
        return annoValue.isEmpty() ? widgetController.getClass().getName() : annoValue;
    }
}
