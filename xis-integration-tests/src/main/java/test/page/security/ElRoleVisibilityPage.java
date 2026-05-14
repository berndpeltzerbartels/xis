package test.page.security;

import one.xis.ModelData;
import one.xis.Page;

import java.util.List;

@Page("/el-role-visibility.html")
class ElRoleVisibilityPage {

    @ModelData("visibleRoles")
    List<String> visibleRoles() {
        return List.of("SUPPORT", "ADMIN");
    }
}
