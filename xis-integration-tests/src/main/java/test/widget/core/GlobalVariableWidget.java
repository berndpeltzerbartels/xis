package test.widget.core;

import lombok.Getter;
import one.xis.Action;
import one.xis.GlobalVariable;
import one.xis.Widget;

import java.util.ArrayList;
import java.util.List;

@Getter
@Widget
public class GlobalVariableWidget {

    private final List<String> invokedMethods = new ArrayList<>();
    private String widgetData = "initial-widget-data";

    @GlobalVariable("widgetData")
    String widgetData() {
        invokedMethods.add("widgetData");
        return widgetData;
    }

    @GlobalVariable("sharedMessage")
    String sharedMessage() {
        invokedMethods.add("sharedMessage");
        return "Hello from Widget";
    }

    @Action("widget-action")
    @GlobalVariable("widgetData")
    String widgetAction() {
        invokedMethods.add("widgetAction");
        this.widgetData = "updated-widget-data";
        return this.widgetData;
    }

    @Action("use-page-data")
    void usePageData(@GlobalVariable("pageData") String pageData) {
        invokedMethods.add("usePageData");
        this.widgetData = "widget-received: " + pageData;
    }
}