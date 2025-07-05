package one.xis.idp;

import lombok.Setter;
import one.xis.*;
import one.xis.auth.InvalidCredentialsException;
import one.xis.auth.token.StateParameter;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static one.xis.idp.XisIDPConfig.IDP_LOGIN_URL;


@Setter
@Page(IDP_LOGIN_URL)
@HtmlFile("/idp-login.html")
@DefaultHtmlFile("/default-idp-login.html")
class IDPLoginController {

    private IDPAuthenticationService authenticationService;
    private Collection<ExternalIDPService> externalIDPServices;

    @ModelData("displayLoginForm")
    boolean displayLoginForm() {
        return authenticationService != null;
    }

    @ModelData("externalIdpIds")
    Collection<String> getExternalIdpIds() {
        return externalIDPServices.stream()
                .map(ExternalIDPService::getProviderId)
                .toList();
    }

    @ModelData("externalIdpUrls")
    Map<String, String> getExternalIdpUrls(@URLParameter("redirect_url") String postLoginRedirectUrl) {
        return externalIDPServices.stream()
                .collect(Collectors.toMap(ExternalIDPService::getProviderId, service -> service.createLoginUrl(postLoginRedirectUrl)));
    }

    @FormData("login")
    IDPServerLogin createLoginFormData(@URLParameter("state") String state) {
        StateParameter.decodeAndVerify(state);
        return new IDPServerLogin(null, null, state);
    }

    @Action("login")
    public IDPServerLoginResponse login(@FormData("login") IDPServerLogin login) throws InvalidCredentialsException {
        if (authenticationService == null) {
            throw new IllegalStateException("Local authentication is not present. This may be because no implementation of " + IPDService.class + " is available.");
        }
        String code;
        var payload = StateParameter.decodeAndVerify(login.getState());
        // Logic for handling login action
        try {
            code = authenticationService.login(login);
            authenticationService.checkRedirectUrl(login.getUsername(), payload.getRedirect());
        } catch (InvalidCredentialsException e) {
            throw new IllegalArgumentException("Invalid redirect URL", e);
        }
        return new IDPServerLoginResponse(payload.getRedirect(), login.getState(), code);
    }

}