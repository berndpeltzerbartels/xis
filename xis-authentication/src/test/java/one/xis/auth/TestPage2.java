package one.xis.auth;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.Roles;

@Page("/protected.html")
@Roles("admin")
class TestPage2 {
    @ModelData("message")
    String getMessage() {
        return "This should not be returned";
    }
}
