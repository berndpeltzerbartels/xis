package test.widget.action;

import lombok.RequiredArgsConstructor;
import one.xis.ModelData;
import one.xis.Page;

@Page("/actionFormPage.html")
@RequiredArgsConstructor
public class ActionFormPage {
    @ModelData("widgetId")
    public String widgetId() {
        return "ActionFormWidget";
    }
}