package one.xis.auth;

import lombok.RequiredArgsConstructor;
import one.xis.auth.token.StateParameter;
import one.xis.auth.token.StateParameterPayload;
import one.xis.auth.token.TokenService;
import one.xis.context.XISInit;
import one.xis.http.*;
import one.xis.ipdclient.IDPClientService;
import one.xis.resource.Resources;
import one.xis.server.ServerResponse;
import one.xis.utils.http.HttpUtils;

import java.util.Collections;
import java.util.Set;

/**
 * Controller for handling local authentication callbacks.
 * This controller is responsible for processing the authentication callback for external identity providers (IDPs).
 * It will not handle local logins.
 */
@Controller("/xis/auth")
@RequiredArgsConstructor
class LocalAuthenticationController {

    private final IDPClientService idpClientService;
    private final TokenService tokenService;
    private final UserInfoService<?> userInfoService;
    private final Resources resources;

    private String loginHtmlTemplate;

    @XISInit
    void initLoginForm() {
        if (resources.exists("/login.html")) {
            loginHtmlTemplate = resources.getByPath("/login.html").getContent();
        } else {
            loginHtmlTemplate = resources.getByPath("/default-login.html").getContent();
        }
    }

    @Get("/login.html")
    @Produces(ContentType.TEXT_HTML)
    public String loginForm(@UrlParameter("redirect_uri") String redirectUri, HttpRequest request) {
        var state = StateParameter.create(redirectUri); // TODO code !!!
        var invalid = request.getFormParameters().containsKey("invalid");
        return loginHtmlTemplate.replace("${state}", state).replace("${invalid}", String.valueOf(invalid));
    }

    /*
    @Post("/login")
    ResponseEntity<?> login(@RequestBody(BodyType.FORM_URLENCODED) LocalLoginData localLoginData) {
        if (!userInfoService.validateCredentials(localLoginData.getUsername(), localLoginData.getPassword())) {
            var payload = StateParameter.decode(localLoginData.getState());
            if (isExpired(payload)) {
                return ResponseEntity.status(302).addHeader("Location", HttpUtils.localizeUrl(payload.getRedirect()));
            }
            var userInfo = userInfoService.getUserInfo(localLoginData.getUsername()).orElseThrow();
            var tokens = tokenService.newTokens(userInfo);
            return ResponseEntity.status(302)
                    .addHeader("Location", HttpUtils.localizeUrl(payload.getRedirect()))
                    .addSecureCookie("access_token", tokens.getAccessToken(), tokens.getAccessTokenExpiresIn())
                    .addSecureCookie("refresh_token", tokens.getRenewToken(), tokens.getRenewTokenExpiresIn());
        }
        return ResponseEntity.status(302).addHeader("Location", "/login.html?invalid=true");
    }
    */

    private boolean isExpired(StateParameterPayload payload) {
        return payload.getExpiresAtSeconds() > System.currentTimeMillis() / 1000;
    }

    /**
     * Handles the authentication callback from an IDP.
     * It retrieves the authorization code and state from the request parameters,
     * decodes and verifies the state, fetches new tokens using the IDP client service,
     * and returns the tokens along with a redirect URL.
     *
     * @param code  The authorization code received from the IDP.
     * @param state The state parameter used to verify the request.
     * @param idpId The ID of the identity provider.
     * @return A response entity containing the API tokens and redirect URL.
     */
    @Get("/callback/{idpId}")
    public ResponseEntity<?> authenticationCallback(@UrlParameter("code") String code, @UrlParameter("state") String state, @PathVariable("idpId") String idpId) {
        var stateParameterPayload = StateParameter.decodeAndVerify(state);
        var tokens = tokenService.newTokens("bla", Set.of("admin"), Collections.emptyMap());
        var serverResponse = new ServerResponse();
        serverResponse.setRedirectUrl(HttpUtils.localizeUrl(stateParameterPayload.getRedirect()));
        return ResponseEntity.ok(serverResponse)
                .addSecureCookie("access_token", tokens.getAccessToken(), tokens.getAccessTokenExpiresIn())
                .addSecureCookie("refresh_token", tokens.getRenewToken(), tokens.getRenewTokenExpiresIn());
    }


    /**
     * Renews the access and refresh tokens using the provided refresh token.
     * It decodes the refresh token, retrieves the user information, and generates new tokens.
     *
     * @param refreshToken The refresh token used to renew the tokens.
     * @return A response entity with no content, but with updated secure cookies for access and refresh tokens.
     */
    @Post("/token")
    public ResponseEntity<?> renewTokens(@CookieValue("refresh_token") String refreshToken) {
        var tokenAttributes = tokenService.decodeToken(refreshToken);
        var userInfo = userInfoService.getUserInfo(tokenAttributes.userId())
                .orElseThrow(() -> new IllegalStateException("User not found for token: " + refreshToken));
        var tokens = tokenService.newTokens(userInfo);
        return ResponseEntity.noContent()
                .addSecureCookie("access_token", tokens.getAccessToken(), tokens.getAccessTokenExpiresIn())
                .addSecureCookie("refresh_token", tokens.getRenewToken(), tokens.getRenewTokenExpiresIn());
    }

}
