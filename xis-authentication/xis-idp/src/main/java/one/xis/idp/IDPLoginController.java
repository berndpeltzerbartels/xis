package one.xis.idp;

import lombok.Setter;
import one.xis.*;
import one.xis.auth.InvalidCredentialsException;
import one.xis.auth.InvalidRedirectUrlException;
import one.xis.auth.token.StateParameter;
import one.xis.validation.Validator;
import one.xis.validation.ValidatorException;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static one.xis.idp.XisIDPConfig.IDP_LOGIN_URL;


@Setter
@Page(IDP_LOGIN_URL)
@HtmlFile("/idp-login.html")
@DefaultHtmlFile("/default-idp-login.html")
class IDPLoginController implements Validator<IDPServerLogin> {

    private IDPAuthenticationService idpAuthenticationService;
    private Collection<ExternalIDPService> externalIDPServices;

    @ModelData("displayLoginForm")
    boolean displayLoginForm() {
        return idpAuthenticationService != null;
    }

    @ModelData("externalIdpIds")
    Collection<String> getExternalIdpIds() {
        return externalIDPServices.stream()
                .map(ExternalIDPService::getProviderId)
                .toList();
    }

    @ModelData("externalIdpUrls")
    Map<String, String> getExternalIdpUrls(@URLParameter("redirect_uri") String postLoginRedirectUrl) { // Annotation korrigiert
        return externalIDPServices.stream()
                .collect(Collectors.toMap(ExternalIDPService::getProviderId, service -> service.createLoginUrl(postLoginRedirectUrl)));
    }

    @FormData("login")
    IDPServerLogin createLoginFormData(@URLParameter("state") String state, @URLParameter("redirect_uri") String redirectUrl) {
        StateParameter.decodeAndVerify(state);
        return new IDPServerLogin(null, null, state, redirectUrl);
    }

    @Action("login")
    public IDPServerLoginResponse login(@FormData("login") IDPServerLogin login) throws InvalidCredentialsException {
        StateParameter.decodeAndVerify(login.getState());
        // Logic for handling login action
        String code;
        try {
            code = idpAuthenticationService.login(login);
            idpAuthenticationService.checkRedirectUrl(login.getUsername(), login.getRedirectUri());
        } catch (InvalidCredentialsException e) {
            throw new IllegalArgumentException("Invalid redirect URL", e);
        }
        return new IDPServerLoginResponse(login.getRedirectUri(), login.getState(), code);
    }

    @Override
    public void validate(IDPServerLogin login, AnnotatedElement annotatedElement) throws ValidatorException {
        if (idpAuthenticationService == null) {
            throw new IllegalStateException("Local authentication is not present. This may be because no implementation of " + IDPService.class + " is available.");
        }
        // Logic for handling login action
        try {
            idpAuthenticationService.login(login);
            idpAuthenticationService.checkRedirectUrl(login.getUsername(), login.getRedirectUri());
        } catch (InvalidCredentialsException | InvalidRedirectUrlException e) {
            throw new ValidatorException();
        }
    }
}