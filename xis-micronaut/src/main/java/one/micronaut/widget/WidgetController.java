package one.micronaut.widget;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.micronaut.micronaut.MicronautContextAdapter;
import one.xis.widget.WidgetService;

@Controller
class WidgetController {

    @Inject
    private MicronautContextAdapter adapter;
    private WidgetService widgetService;

    @PostConstruct
    void init() {
        widgetService = adapter.getWidgetService();
    }


    @Get(produces = "text/javascript; charset=utf-8", uri = "/xis/widget/{widgetId}")
    String getWidget(@PathVariable("widgetId") String widgetId) {
        return widgetService.getWidgetJavascript(widgetId).getContent();
    }

}