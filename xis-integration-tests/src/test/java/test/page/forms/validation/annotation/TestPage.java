package test.page.forms.validation.annotation;

import one.xis.Action;
import one.xis.FormData;
import one.xis.HtmlFile;
import one.xis.Page;

@Page("/test.html")
@HtmlFile("/TestPage.html")
public class TestPage {

    @Action
    void save(@FormData("formObject") AnnotationValidationTest.TestModel model) {
        // This method is intentionally left empty to simulate a save action
        // The validation messages will be triggered by the NotNegative annotation in the model
    }
}
