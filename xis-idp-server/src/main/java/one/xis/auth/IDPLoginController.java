package one.xis.auth;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.*;
import one.xis.validation.Validator;
import one.xis.validation.ValidatorException;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;


@Setter
@Page("/idp/login.html")
@HtmlFile("idp-login.html")
@DefaultHtmlFile("/default-idp-login.html")
@RequiredArgsConstructor
class IDPLoginController implements Validator<IDPServerLogin> {

    private final IDPAuthenticationService idpAuthenticationService;
    private final IDPService idpService;

    /**
     * .append("?response_type=code")
     * .append("&redirect_uri=").append(getAuthenticationCallbackUrl())
     * .append("&state=").append(stateParameter)
     * .append("&nonce=").append(SecurityUtil.createRandomKey(32))
     * .append("&client_id=").append(providerConfiguration.getClientId());
     *
     * @param state
     * @param redirectUri
     * @return
     */

    @FormData("login")
    IDPServerLogin createLoginFormData(@URLParameter("client_id") String clientId, @URLParameter("state") String state, @URLParameter("redirect_uri") String redirectUri) {
        var clientInfo = idpService.findClientInfo(clientId).orElseThrow(() -> new AuthenticationException("invalid client-id: " + clientId));
        if (!clientInfo.getPermittedRedirectUrls().contains(redirectUri)) {
            throw new AuthenticationException("invalid redirect-uri: " + redirectUri);
        }
        return new IDPServerLogin(null, null, state, redirectUri);
    }

    @Action("login")
    public PageUrlResponse login(@FormData("login") IDPServerLogin login) throws InvalidCredentialsException {
        // Logic for handling login action
        String code;
        try {
            code = idpAuthenticationService.login(login);
            idpAuthenticationService.checkRedirectUrl(login.getUsername(), login.getRedirectUri());
        } catch (InvalidCredentialsException e) {
            throw new IllegalArgumentException("Invalid redirect URL", e);
        }
        return new PageUrlResponse(login.getRedirectUri(), Map.of("code", code, "state", login.getState()));
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