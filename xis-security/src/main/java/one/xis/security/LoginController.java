package one.xis.security;

import one.xis.DefaultHtmlFile;
import one.xis.HtmlFile;
import one.xis.Page;


@Page(LoginController.URL)
@HtmlFile("/login.html")
@DefaultHtmlFile("/default-login.html")
class LoginController {
    public static final String URL = "/login";


}
