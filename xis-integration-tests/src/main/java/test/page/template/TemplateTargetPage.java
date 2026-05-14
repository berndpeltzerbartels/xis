package test.page.template;

import one.xis.ModelData;
import one.xis.Page;

@Page("/template-target.html")
class TemplateTargetPage {

    @ModelData("title")
    String title() {
        return "Template Target";
    }
}
