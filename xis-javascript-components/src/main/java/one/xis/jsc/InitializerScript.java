package one.xis.jsc;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
class InitializerScript {

    private final PageJavascripts pageJavascripts;
    private final Widgets widgets;

    String getContent() {
        return getPagesRegistrationJs() + getWidgetsRegistrationJs();
    }

    private String getPagesRegistrationJs() {
        StringBuilder s = new StringBuilder();
        pageJavascripts.getAll().forEach((key, pageJavascript) -> s.append(getPageRegistrationJs(key, pageJavascript)));
        return s.toString();
    }

    private String getWidgetsRegistrationJs() {
        StringBuilder s = new StringBuilder();
        widgets.getAll().forEach((key, widgetJavascript) -> s.append(getWidgetRegistrationJs(key, widgetJavascript)));
        return s.toString();
    }

    private String getPageRegistrationJs(String key, PageJavascript pageJavascript) {
        return String.format("pageJavascripts.addPage('%s', new %s());\n", key, pageJavascript.getJavascriptClass());
    }

    private String getWidgetRegistrationJs(String key, WidgetJavascript widgetJavascript) {
        return String.format("widgets.addWidget('%s', new %s());\n", key, widgetJavascript.getJavascriptClass());
    }
}
