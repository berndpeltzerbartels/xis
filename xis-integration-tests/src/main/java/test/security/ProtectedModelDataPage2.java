package test.security;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.Roles;
import one.xis.UserId;

@Page("/page2.html")
public class ProtectedModelDataPage2 {

    @ModelData("userId")
    String userId(@UserId String userId) {
        return userId;
    }

    @Roles("admin")
    @ModelData("admin-value")
    String value2() {
        return "xyz";
    }
}
