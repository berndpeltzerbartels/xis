package one.xis.security;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import one.xis.*;
import one.xis.context.XISInit;
import one.xis.server.ApiTokens;
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

    @Action("login")
    public ApiTokens login(@FormData("login") LoginFormData login) {
        if (localAuthentication == null) {
            throw new IllegalStateException("Local authentication is not configured. This may be because no implementation of UserService is available.");
        }
        // Logic for handling login action
        return localAuthentication.login(login.username, login.password);
    }

    @Data
    static class LoginFormData {
        private String username;
        private String password;
        private String redirect;

        // Getters and setters can be added here if needed
    }

}
