package one.xis.auth;

import lombok.RequiredArgsConstructor;
import one.xis.auth.token.StateParameter;
import one.xis.auth.token.TokenService;
import one.xis.http.*;
import one.xis.ipdclient.IDPClientService;
import one.xis.utils.http.HttpUtils;

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
        var tokens = idpClientService.fetchNewTokens(idpId, code, state);
        return ResponseEntity.redirect(HttpUtils.localizeUrl(stateParameterPayload.getRedirect()))
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
