package one.xis.security;

import lombok.RequiredArgsConstructor;
import one.xis.*;
import one.xis.context.XISInit;

import java.util.List;

@Page("/idp/login.html")
@HtmlFile("/idp-login.html")
@DefaultHtmlFile("/default-idp-login.html")
@RequiredArgsConstructor
class IDPLoginController {
    private IDPService idpService;

    @XISInit
    void init(List<IDPService> idpServices) {
        idpService = switch (idpServices.size()) {
            case 0 -> null;
            case 1 -> idpServices.get(0);
            default ->
                    throw new IllegalStateException("Multiple " + IDPService.class.getSimpleName() + " implementations found. Please ensure only one is configured.");
        };
    }

    @FormData("login")
    IDPLogin createLoginFormData(@URLParameter("state") String state, @URLParameter("redirect_url") String redirectUrl) {
        return new IDPLogin(null, null, state, redirectUrl);
    }

    @Action("login")
    public IDPLoginResponse login(@FormData("login") IDPLogin login) {
        if (idpService == null) {
            throw new IllegalStateException("Local authentication is not present. This may be because no implementation of " + LocalUserInfoService.class + " is available.");
        }
        var payload = StateParameter.decodeAndVerify(login.getState());
        // Logic for handling login action
        var code = idpService.login(login);
        idpService.checkRedirectUrl(login.getRedirectUrl());
        return new IDPLoginResponse(login.getRedirectUrl(), login.getState(), code);
    }

}