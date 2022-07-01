package one.xis.micronaut;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;

@Controller("/widget.js")
class WidgetController {

    @Inject
    private WidgetInitializer widgetInitializer;

    @Get(produces = "text/javascript")
    String getWidget() {
        return "";
    }
}