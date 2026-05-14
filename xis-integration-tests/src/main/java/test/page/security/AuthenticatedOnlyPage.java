package test.page.security;

import one.xis.Action;
import one.xis.Authenticated;
import one.xis.ModelData;
import one.xis.Page;

@Page("/authenticated-only.html")
@Authenticated
public class AuthenticatedOnlyPage {

    @ModelData
    String pageData() {
        return "Authenticated page accessible";
    }

    @Action
    void authenticatedAction() {
        // The controller already requires an authenticated user.
    }
}
