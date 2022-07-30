package one.xis.js;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.page.PageJavascript;
import one.xis.page.PageJavascripts;
import one.xis.widget.WidgetJavascript;
import one.xis.widget.WidgetJavascripts;

@XISComponent
@RequiredArgsConstructor
class InitializerScript {

    private final PageJavascripts pageJavascripts;
    private final WidgetJavascripts widgetJavascripts;

    String getContent() {
        return getPagesRegistrationJs() +
                getWidgetsRegistrationJs() +
                getWelcomPageJs() +
                getInitPagesJs();
    }

    private String getPagesRegistrationJs() {
        StringBuilder s = new StringBuilder();
        pageJavascripts.getAll().forEach((key, pageJavascript) -> s.append(getPageRegistrationJs(key, pageJavascript)));
        return s.toString();
    }

    private String getWidgetsRegistrationJs() {
        StringBuilder s = new StringBuilder();
        widgetJavascripts.getAll().forEach((key, widgetJavascript) -> s.append(getWidgetRegistrationJs(key, widgetJavascript)));
        return s.toString();
    }

    private String getPageRegistrationJs(String key, PageJavascript pageJavascript) {
        return String.format("pages.addPage('%s', new %s());\n", key, pageJavascript.getJavascriptClass());
    }

    private String getWidgetRegistrationJs(String key, WidgetJavascript widgetJavascript) {
        return String.format("widgets.addWidget('%s', new %s());\n", key, widgetJavascript.getJavascriptClass());
    }

    private String getWelcomPageJs() {
        if (pageJavascripts.getWelcomePage() != null) {
            return String.format("pages.setWelcomePage('%s');\n", pageJavascripts.getWelcomePage().getKey());
        }
        return "";
    }

    private String getInitPagesJs() {
        return "pages.init();";
    }
}
