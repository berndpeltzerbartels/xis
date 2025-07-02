package one.xis.idp;

import one.xis.*;
import one.xis.auth.InvalidCredentialsException;
import one.xis.auth.token.StateParameter;
import one.xis.context.XISInject;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static one.xis.idp.IDPLoginController.IDP_LOGIN_URL;


@Page(IDP_LOGIN_URL)
@HtmlFile("/idp-login.html")
@DefaultHtmlFile("/default-idp-login.html")
class IDPLoginController {

    static final String IDP_LOGIN_URL = "/idp/login.html";

    @XISInject(optional = true)
    private LocalIDPService localIDPService;

    @XISInject
    private Collection<ExternalIDPService> externalIDPServices;

    @ModelData("displayLoginForm")
    boolean displayLoginForm() {
        return localIDPService != null;
    }

    @ModelData("externalIdpIds")
    Collection<String> getExternalIdpIds() {
        return externalIDPServices.stream()
                .map(ExternalIDPService::getProviderId)
                .toList();
    }

    @ModelData("externalIdpUrls")
    Map<String, String> getExternalIdpUrls(@URLParameter("redirect_url") String redirectUrl) {
        return externalIDPServices.stream()
                .collect(Collectors.toMap(ExternalIDPService::getProviderId, service -> service.createLoginUrl(redirectUrl)));
    }


    @FormData("login")
    LocalIDPLogin createLoginFormData(@URLParameter("state") String state, @URLParameter("redirect_url") String redirectUrl) {
        StateParameter.decodeAndVerify(state);
        localIDPService.checkRedirectUrl(redirectUrl);
        return new LocalIDPLogin(null, null, state, redirectUrl);
    }

    @Action("login")
    public LocalIDPLoginResponse login(@FormData("login") LocalIDPLogin login) throws InvalidCredentialsException {
        if (localIDPService == null) {
            throw new IllegalStateException("Local authentication is not present. This may be because no implementation of " + LocalIDPUserService.class + " is available.");
        }
        String code;
        StateParameter.decodeAndVerify(login.getState());
        // Logic for handling login action
        try {
            code = localIDPService.login(login);
            localIDPService.checkRedirectUrl(login.getRedirectUrl());
        } catch (InvalidCredentialsException e) {
            throw new IllegalArgumentException("Invalid redirect URL", e);
        }
        return new LocalIDPLoginResponse(login.getRedirectUrl(), login.getState(), code);
    }

}