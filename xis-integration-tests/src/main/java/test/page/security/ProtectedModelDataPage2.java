package test.page.security;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.Roles;

@Page("/page2.html")
public class ProtectedModelDataPage2 {

    @Roles("admin")
    @ModelData("adminData")
    String value2() {
        return "xyz";
    }
}
