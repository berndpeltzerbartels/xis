package test.page.security;

import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.Roles;

@Page("/no-controller-roles.html")
public class NoControllerRolesTestPage {

    @ModelData
    String pageData() {
        return "Page accessible";
    }

    @Action
    void actionWithoutRoles() {
        // No roles required
    }

    @Action
    @Roles({"ADMIN", "MODERATOR"})
    void actionWithMethodRolesOnly() {
        // Requires: (ADMIN OR MODERATOR) - no controller roles
    }

    @Action
    @FormData("data")
    void actionWithDtoRolesOnly(ProtectedDto dto) {
        // Requires: (DATA_EDITOR OR CONTENT_MANAGER) - no controller or method roles
    }
}
