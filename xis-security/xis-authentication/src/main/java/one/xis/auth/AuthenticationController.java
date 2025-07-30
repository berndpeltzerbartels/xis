package one.xis.auth;

import lombok.RequiredArgsConstructor;
import one.xis.auth.idp.ExternalIDPServices;
import one.xis.context.AppContext;
import one.xis.http.*;
import one.xis.server.ServerResponse;
import one.xis.utils.http.HttpUtils;

/**
 * Controller for handling local authentication callbacks.
 * This controller is responsible for processing the authentication callback for external identity providers (IDPs).
 * It will not handle local logins.
 */
@Controller("/xis/auth")
@RequiredArgsConstructor
class AuthenticationController {

    private final TokenService tokenService;
    private final AppContext appContext;
    private final ExternalIDPServices externalIDPServices;
    private final CodeStore codeStore;


    /**
     * Handles the authentication callback from an IDP.
     * It retrieves the authorization code and state from the request parameters,
     * decodes and verifies the state, fetches new tokens using the IDP client service,
     * and returns the tokens along with a redirect URL.
     * <p>
     * Because this endpoint is called by ajax, it does not return a redirect response.
     * The client has to handle the redirect programmatically, so instead of returning a redirect,
     * it returns a response entity with the redirect URL and tokens in secure cookies.
     *
     * @param code  The authorization code received from the IDP.
     * @param state The state parameter used to verify the request.
     * @return A response entity containing the API tokens and redirect URL.
     */
    @Get("/callback/{idpId}")
    public ResponseEntity<?> authenticationCallback(@UrlParameter("code") String code, @UrlParameter("state") String state) {
        var stateParameterPayload = StateParameter.decodeAndVerify(state);

        var serverResponse = new ServerResponse();
        serverResponse.setRedirectUrl(HttpUtils.localizeUrl(stateParameterPayload.getRedirect()));
        if ("local".equals(stateParameterPayload.getProviderId())) {
            var userId = codeStore.getUserIdForCode(code);
            if (userId == null) {
                throw new AuthenticationException("No user found for code: " + code);
            }
            // Local login, no IDP involved
            var userInfoService = appContext.getOptionalSingleton(UserInfoService.class)
                    .orElseThrow(() -> new IllegalStateException("UserInfoService is required for local authentication"));
            var userInfo = (UserInfo) userInfoService.getUserInfo(userId).orElseThrow();
            var tokens = tokenService.newTokens(userInfo);
            return ResponseEntity.redirect(stateParameterPayload.getRedirect())
                    .addSecureCookie("access_token", tokens.getAccessToken(), tokens.getAccessTokenExpiresIn())
                    .addSecureCookie("refresh_token", tokens.getRenewToken(), tokens.getRenewTokenExpiresIn());

        } else {
            // IDP login, use the IDP client service to fetch tokens
            var externalIDPService = externalIDPServices.getExternalIDPService(stateParameterPayload.getProviderId());
            if (externalIDPService == null) {
                throw new IllegalStateException("No IDP client service found for provider: " + stateParameterPayload.getProviderId());
            }
            var externalTokens = externalIDPService.fetchTokens(code);
            return ResponseEntity.redirect(stateParameterPayload.getRedirect()).addSecureCookie("access_token", externalTokens.getAccessToken(), externalTokens.getExpiresInSeconds())
                    .addSecureCookie("refresh_token", externalTokens.getRefreshToken(), externalTokens.getRefreshExpiresInSeconds());

        }
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
        var renewTokenClaims = tokenService.decodeRenewToken(refreshToken);
        @SuppressWarnings("unchecked") var userInfoService = (UserInfoService<UserInfo>) appContext.getOptionalSingleton(UserInfoService.class).orElseThrow(() -> new IllegalStateException("UserInfoService is required for token renewal"));
        var userInfo = userInfoService.getUserInfo(renewTokenClaims.getUserId())
                .orElseThrow(() -> new AuthenticationException("User not found for userId: " + renewTokenClaims.getUserId()));
        var tokens = tokenService.newTokens(userInfo);
        return ResponseEntity.noContent()
                .addSecureCookie("access_token", tokens.getAccessToken(), tokens.getAccessTokenExpiresIn())
                .addSecureCookie("refresh_token", tokens.getRenewToken(), tokens.getRenewTokenExpiresIn());
    }


}
