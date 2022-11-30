package one.xis.root;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.page.PageComponent;
import one.xis.page.PageService;
import one.xis.widget.WidgetComponent;
import one.xis.widget.WidgetService;

@XISComponent
@RequiredArgsConstructor
class InitializerScript {

    private final PageService pageService;
    private final WidgetService widgetService;

    String getContent() {
        return getPagesRegistrationJs() +
                getWidgetsRegistrationJs() +
                getWelcomPageJs();
    }

    private String getPagesRegistrationJs() {
        StringBuilder s = new StringBuilder();
        pageService.getPageComponentsByPath().forEach((key, pageComponent) -> s.append(getPageRegistrationJs(key, pageComponent)));
        return s.toString();
    }

    private String getWidgetsRegistrationJs() {
        StringBuilder s = new StringBuilder();
        widgetService.getAllWidgetJavascripts().forEach((key, widgetComponent) -> s.append(getWidgetRegistrationJs(key, widgetComponent)));
        return s.toString();
    }

    private String getPageRegistrationJs(@NonNull String path, PageComponent pageComponent) {
        return String.format("pages.addPage('%s', new %s());\n", path, pageComponent.getJavascriptClass());
    }

    private String getWidgetRegistrationJs(@NonNull String key, WidgetComponent widgetComponent) {
        return String.format("widgets.addWidget('%s', new %s());\n", key, widgetComponent.getJavascriptClass());
    }

    private String getWelcomPageJs() {
        if (pageService.getWelcomePageJavascript() != null) {
            return String.format("pages.setWelcomePage('%s');\n", pageService.getWelcomePageJavascript().getPath());
        }
        return "";
    }
}