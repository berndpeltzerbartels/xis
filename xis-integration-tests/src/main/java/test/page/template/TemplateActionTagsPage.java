package test.page.template;

import one.xis.Action;
import one.xis.ActionParameter;
import one.xis.ModelData;
import one.xis.Page;

@Page("/template-action-tags.html")
class TemplateActionTagsPage {

    private String actionResult = "none";
    private int buttonClicks;

    @ModelData("actionResult")
    String actionResult() {
        return actionResult;
    }

    @ModelData("buttonClicks")
    int buttonClicks() {
        return buttonClicks;
    }

    @Action("tag-action")
    void tagAction(@ActionParameter("value") String value) {
        actionResult = value;
    }

    @Action("button-action")
    void buttonAction() {
        buttonClicks++;
    }
}
