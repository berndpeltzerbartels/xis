package test.page.security;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.Roles;

@Page("/mixed")
class MixedPage {


    @Roles("user")
    @ModelData("userOnlyPage")
    public String userOnlyPage() {
        return "This page is accessible only to users with the 'user' role.";
    }
}