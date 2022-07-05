package one.xis.jsc;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

@Controller
class WidgetController {

    @Inject
    private MicronautContextAdapter contextAdapter;

    private Widgets widgets;

    @PostConstruct
    void init() {
        widgets = contextAdapter.getWidgets();
    }

    @Get(produces = "text/javascript; charset=utf-8", uri = "/xis/widget/{widgetUrn}")
    String getWidget(@PathVariable("widgetUrn") String widgetUrn) {
        return widgets.get(widgetUrn).getJavascript();
    }

}