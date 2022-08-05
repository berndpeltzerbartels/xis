package one.xis.root;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.page.PageJavascript;
import one.xis.page.PageService;
import one.xis.widget.WidgetJavascript;
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
        pageService.getPagesByPath().forEach((key, pageJavascript) -> s.append(getPageRegistrationJs(key, pageJavascript)));
        return s.toString();
    }

    private String getWidgetsRegistrationJs() {
        StringBuilder s = new StringBuilder();
        widgetService.getAllWidgetJavascripts().forEach((key, widgetJavascript) -> s.append(getWidgetRegistrationJs(key, widgetJavascript)));
        return s.toString();
    }

    private String getPageRegistrationJs(@NonNull String path, PageJavascript pageJavascript) {
        return String.format("pages.addPage('%s', new %s());\n", path, pageJavascript.getJavascriptClass());
    }

    private String getWidgetRegistrationJs(@NonNull String key, WidgetJavascript widgetJavascript) {
        return String.format("widgets.addWidget('%s', new %s());\n", key, widgetJavascript.getJavascriptClass());
    }

    private String getWelcomPageJs() {
        if (pageService.getWelcomePageJavascript() != null) {
            return String.format("pages.setWelcomePage('%s');\n", pageService.getWelcomePageJavascript().getPath());
        }
        return "";
    }
}