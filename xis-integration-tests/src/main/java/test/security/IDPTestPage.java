package test.security;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.Roles;

@Page("/local-authentication.html")
class IDPTestPage {

    @Roles("admin")
    @ModelData("localAuthentication")
    String model() {
        return "localAuthentication";
    }
}
