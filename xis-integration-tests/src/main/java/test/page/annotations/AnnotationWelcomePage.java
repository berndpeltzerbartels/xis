package test.page.annotations;

import one.xis.DefaultHtmlFile;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.UserId;
import one.xis.WelcomePage;

@WelcomePage
@DefaultHtmlFile("AnnotationWelcomeTemplate")
@Page("/annotation-welcome.html")
class AnnotationWelcomePage {

    @ModelData("message")
    String message() {
        return "Welcome annotation resolved";
    }

    @ModelData("userId")
    String userId(@UserId String userId) {
        return userId;
    }
}
