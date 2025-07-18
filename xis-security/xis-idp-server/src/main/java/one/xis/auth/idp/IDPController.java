package one.xis.auth.idp;

import lombok.RequiredArgsConstructor;
import one.xis.auth.AuthenticationException;
import one.xis.auth.token.TokenService;
import one.xis.http.*;

@Controller
@RequiredArgsConstructor
class IDPController {

    private final IDPCodeStore codeStore;
    private final IDPService idpService;
    private final TokenService tokenService;

    @Post("/xis/idp/tokens")
    ResponseEntity<IDPTokenResponse> fetchNewTokens(@RequestBody(BodyType.FORM_URLENCODED) IDPTokenRequest request) throws AuthenticationException {
        if (!idpService.validateClientSecret(request.getClientId(), request.getClientSecret())) {
            throw new AuthenticationException("Invalid client credentials");
        }
        var userId = codeStore.getUserIdForCode(request.getCode());
        if (userId == null) {
            throw new AuthenticationException("Invalid or expired code");
        }
        codeStore.invalidate(request.getCode());
        var userInfo = idpService.findUserInfo(userId).orElseThrow(() -> new AuthenticationException("Invalid user-id"));
        if (!userInfo.getClientId().equals(request.getClientId())) {
            throw new AuthenticationException("Invalid client ID for user");
        }
        var tokens = tokenService.newTokens(userInfo);
        var response = new IDPTokenResponse();
        response.setAccessToken(tokens.getAccessToken());
        response.setExpiresIn(tokens.getAccessTokenExpiresIn().getSeconds());
        response.setRefreshToken(tokens.getRenewToken());
        response.setRefreshExpiresIn(tokens.getRenewTokenExpiresIn().getSeconds());
        // response.setScope(); TODO "Profile lesen", "EMail lesen", "Telefonnummer lesen" etc. ?
        return ResponseEntity.ok(response);
    }
}
