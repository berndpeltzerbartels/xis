package test.page.security;

import one.xis.Page;
import one.xis.Roles;

@Page("/admin.html")
@Roles("admin")
class AdminPage {
}
