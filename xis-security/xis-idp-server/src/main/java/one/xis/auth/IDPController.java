package one.xis.auth;

import lombok.RequiredArgsConstructor;
import one.xis.http.*;

@Controller
@RequiredArgsConstructor
class IDPController {

    private final IDPAuthenticationService idpAuthenticationService;

    @Get("/.well-known/openid-configuration")
    @Produces(ContentType.JSON)
    ResponseEntity<IDPWellKnownOpenIdConfig> getOpenIdConfig() {
        return ResponseEntity.ok(idpAuthenticationService.getOpenIdConfigJson());
    }

    @Get("/.well-known/jwks.json")
    @Produces(ContentType.JSON)
    ResponseEntity<JsonWebKey> getPublicJsonWebKey() {
        return ResponseEntity.ok(idpAuthenticationService.getPublicJsonWebKey());
    }

    @Post("/xis/idp/tokens")
    ResponseEntity<IDPTokenResponse> fetchNewTokens(@RequestBody(BodyType.FORM_URLENCODED) IDPTokenRequest request) throws AuthenticationException {
        try {
            IDPTokenResponse response = idpAuthenticationService.provideTokens(request);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(422);
        }
    }


}
