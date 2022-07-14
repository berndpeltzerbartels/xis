package one.xis.jsc;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
class InitializerScript {

    private final Pages pages;
    private final Widgets widgets;

    String getContent() {
        return getPagesRegistrationJs() + getWidgetsRegistrationJs();
    }

    private String getPagesRegistrationJs() {
        StringBuilder s = new StringBuilder();
        pages.getAll().forEach((key, page) -> s.append(getPageRegistrationJs(key, page)));
        return s.toString();
    }

    private String getWidgetsRegistrationJs() {
        StringBuilder s = new StringBuilder();
        widgets.getAll().forEach((key, widget) -> s.append(getWidgetRegistrationJs(key, widget)));
        return s.toString();
    }

    private String getPageRegistrationJs(String key, Page page) {
        return String.format("pages.addPage('%s', new %s());\n", key, page.getJavascriptClass());
    }

    private String getWidgetRegistrationJs(String key, Widget widget) {
        return String.format("widgets.addWidget('%s', new %s());\n", key, widget.getJavascriptClass());
    }
}
