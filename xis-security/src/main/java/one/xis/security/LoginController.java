package one.xis.security;

import lombok.RequiredArgsConstructor;
import one.xis.*;
import one.xis.context.XISInit;
import one.xis.server.FrontendService;

import java.util.List;


@Page(LoginController.URL)
@HtmlFile("/login.html")
@DefaultHtmlFile("/default-login.html")
@RequiredArgsConstructor
class LoginController {
    public static final String URL = "/login";
    private final FrontendService frontendService;
    private LocalAuthentication localAuthentication;

    @XISInit
    void init(List<LocalAuthentication> localAuthentications) {
        localAuthentication = switch (localAuthentications.size()) {
            case 0 -> null;
            case 1 -> localAuthentications.get(0);
            default ->
                    throw new IllegalStateException("Multiple LocalAuthentication implementations found. Please ensure only one is configured.");
        };
    }

    @FormData("login")
    LoginFormData createLoginFormData(@URLParameter("state") String state) {
        return new LoginFormData(null, null, state);
    }

    @Action("login")
    public LocalLoginResponse login(@FormData("login") LoginFormData login) {
        if (localAuthentication == null) {
            throw new IllegalStateException("Local authentication is not present. This may be because no implementation of " + LocalUserInfoService.class + " is available.");
        }
        var payload = StateParameter.decodeAndVerify(login.getState());
        // Logic for handling login action
        return new LocalLoginResponse(localAuthentication.login(login.getUsername(), login.getPassword()), payload.getRedirect());
    }

}
