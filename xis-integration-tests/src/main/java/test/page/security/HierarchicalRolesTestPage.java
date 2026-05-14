package test.page.security;

import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.Roles;

@Page("/hierarchical-roles.html")
@Roles({"USER", "VERIFIED"})
public class HierarchicalRolesTestPage {

    @ModelData
    String pageData() {
        return "Page accessible";
    }

    @Action
    void actionWithControllerRoleOnly() {
        // Only requires controller roles: USER OR VERIFIED
    }

    @Action
    @Roles({"ADMIN", "MODERATOR"})
    void actionWithControllerAndMethodRoles() {
        // Requires: (USER OR VERIFIED) AND (ADMIN OR MODERATOR)
    }

    @Action
    @Roles("SUPPORT")
    @FormData("data")
    void actionWithAllThreeLevels(ProtectedDto dto) {
        // Requires: (USER OR VERIFIED) AND (SUPPORT) AND (DATA_EDITOR OR CONTENT_MANAGER)
    }

    @Action
    @FormData("data")
    void actionWithControllerAndDtoRoles(ProtectedDto dto) {
        // Requires: (USER OR VERIFIED) AND (DATA_EDITOR OR CONTENT_MANAGER)
    }
}
