package one.xis.idp;

import one.xis.*;
import one.xis.context.XISInject;
import one.xis.security.InvalidCredentialsException;
import one.xis.security.StateParameter;

@Page("/idp/login.html")
@HtmlFile("/idp-login.html")
@DefaultHtmlFile("/default-idp-login.html")
class IDPLoginController {

    @XISInject(optional = true)
    private LocalIDPService idpService;


    @ModelData("displayLoginForm")
    boolean displayLoginForm() {
        return idpService != null;
    }

    @FormData("login")
    LocalIDPLogin createLoginFormData(@URLParameter("state") String state, @URLParameter("redirect_url") String redirectUrl) {
        idpService.checkRedirectUrl(redirectUrl);
        return new LocalIDPLogin(null, null, state, redirectUrl);
    }

    @Action("login")
    public LocalIDPLoginResponse login(@FormData("login") LocalIDPLogin login) throws InvalidCredentialsException {
        if (idpService == null) {
            throw new IllegalStateException("Local authentication is not present. This may be because no implementation of " + LocalIDPUserService.class + " is available.");
        }
        String code;
        var payload = StateParameter.decodeAndVerify(login.getState());
        // Logic for handling login action
        try {
            code = idpService.login(login);
            idpService.checkRedirectUrl(login.getRedirectUrl());
        } catch (InvalidCredentialsException e) {
            throw new IllegalArgumentException("Invalid redirect URL", e);
        }
        return new LocalIDPLoginResponse(login.getRedirectUrl(), login.getState(), code);
    }

}