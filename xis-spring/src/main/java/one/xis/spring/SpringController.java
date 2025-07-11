package one.xis.spring;


import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.Setter;
import one.xis.auth.token.ApiTokens;
import one.xis.context.AppContext;
import one.xis.idp.IDPFrontendService;
import one.xis.server.*;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

import static one.xis.server.FrontendService.AUTHENTICATION_PATH;

@Setter
@RestController
@RequestMapping
class SpringController implements FrameworkController<ResponseEntity<ServerResponse>, HttpServletRequest, ResponseEntity<?>> {

    private FrontendService frontendService;
    private AppContext appContext;

    @Override
    @GetMapping("/xis/config")
    public ClientConfig getComponentConfig(HttpServletRequest request) {
        return frontendService.getConfig();
    }

    @Override
    @PostMapping("/xis/page/model")
    public ResponseEntity<ServerResponse> getPageModel(@RequestBody ClientRequest request,
                                                       @CookieValue(name = "access_token", required = false) String accessToken,
                                                       Locale locale) {
        request.setAccessToken(accessToken);
        request.setLocale(locale);
        var serverResponse = frontendService.processModelDataRequest(request);
        return responseEntity(serverResponse);
    }

    @Override
    @PostMapping("/xis/form/model")
    public ResponseEntity<ServerResponse> getFormModel(@RequestBody ClientRequest request,
                                                       @CookieValue(name = "access_token", required = false) String accessToken,
                                                       Locale locale) {
        request.setAccessToken(accessToken);
        request.setLocale(locale);
        var serverResponse = frontendService.processFormDataRequest(request);
        return responseEntity(serverResponse);
    }

    @Override
    @PostMapping("/xis/widget/model")
    public ResponseEntity<ServerResponse> getWidgetModel(@RequestBody ClientRequest request,
                                                         @CookieValue(name = "access_token", required = false) String accessToken,
                                                         Locale locale) {
        request.setAccessToken(accessToken);
        request.setLocale(locale);
        var serverResponse = frontendService.processModelDataRequest(request);
        return responseEntity(serverResponse);
    }

    @Override
    @PostMapping("/xis/page/action")
    public ResponseEntity<ServerResponse> onPageLinkAction(@RequestBody ClientRequest request,
                                                           @CookieValue(name = "access_token", required = false) String accessToken,
                                                           Locale locale) {
        request.setAccessToken(accessToken);
        request.setLocale(locale);
        var serverResponse = frontendService.processActionRequest(request);
        return responseEntity(serverResponse);
    }

    @Override
    @PostMapping("/xis/widget/action")
    public ResponseEntity<ServerResponse> onWidgetLinkAction(@RequestBody ClientRequest request,
                                                             @CookieValue(name = "access_token", required = false) String accessToken,
                                                             Locale locale) {
        request.setAccessToken(accessToken);
        request.setLocale(locale);
        var serverResponse = frontendService.processActionRequest(request);
        return responseEntity(serverResponse);
    }

    @Override
    @PostMapping("/xis/form/action")
    public ResponseEntity<?> onFormAction(@RequestBody ClientRequest request,
                                          @CookieValue(name = "access_token", required = false) String accessToken,
                                          Locale locale) {
        request.setAccessToken(accessToken);
        request.setLocale(locale);
        return responseEntity(frontendService.processActionRequest(request));
    }

    @Override
    @GetMapping("/xis/page/head")
    public String getPageHead(@RequestHeader("uri") String id) {
        return frontendService.getPageHead(id);
    }

    @Override
    @GetMapping("/xis/page/body")
    public String getPageBody(@RequestHeader("uri") String id) {
        return frontendService.getPageBody(id);
    }

    @Override
    @GetMapping("/xis/page/body-attributes")
    public Map<String, String> getBodyAttributes(@RequestHeader("uri") String id) {
        return frontendService.getBodyAttributes(id);
    }

    @Override
    @GetMapping("/xis/widget/html")
    public String getWidgetHtml(@RequestHeader("uri") String id) {
        return frontendService.getWidgetHtml(id);
    }

    @Override
    @GetMapping("/app.js")
    public String getAppJs() {
        return frontendService.getAppJs();
    }

    @Override
    @GetMapping("/classes.js")
    public String getClassesJs() {
        return frontendService.getClassesJs();
    }

    @Override
    @GetMapping("/main.js")
    public String getMainJs() {
        return frontendService.getMainJs();
    }

    @Override
    @GetMapping("/functions.js")
    public String getFunctionsJs() {
        return frontendService.getFunctionsJs();
    }

    @Override
    @GetMapping("/bundle.min.js")
    public String getBundleJs() {
        return frontendService.getBundleJs();
    }

    @Override
    @GetMapping("/.well-known/openid-configuration")
    public ResponseEntity<?> getOpenIdConfiguration() {
        return appContext.getOptionalSingleton(IDPFrontendService.class).map(
                        idpFrontendService -> ResponseEntity.ok(idpFrontendService.getOpenIdConfigJson()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<?> getIdpPublicKey() {
        return appContext.getOptionalSingleton(IDPFrontendService.class).map(
                        idpFrontendService -> ResponseEntity.ok(idpFrontendService.getPublicKey()))
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @Override
    @GetMapping(AUTHENTICATION_PATH)
    public ResponseEntity<?> authenticationCallback(@RequestParam("code") String code, @RequestParam("state") String state, @PathVariable("idpId") String idpId) {
        var tokensAndUrl = frontendService.authenticationCallback(code, state);
        return addTokenCookies(ResponseEntity.status(302) // Not an ajax request. We are using a real browser redirect.
                .header("Location", tokensAndUrl.getUrl()), tokensAndUrl.getApiTokens()).build();
    }

    @Override
    @PostMapping(value = "/xis/auth/tokens", consumes = "application/x-www-form-urlencoded", produces = "application/json")
    public ResponseEntity<?> getIdpTokens(@RequestBody String body) {
        return appContext.getOptionalSingleton(IDPFrontendService.class)
                .map(idpFrontendService -> {
                    try {
                        var idpResponse = idpFrontendService.provideTokens(body);
                        var responseBuilder = ResponseEntity.ok();
                        addTokenCookies(responseBuilder, idpResponse.getApiTokens());
                        return responseBuilder.body(idpResponse.toOAuth2Response());
                    } catch (Exception e) {
                        // Hier könnten Sie spezifischere Exceptions fangen und entsprechende Fehler-Responses generieren
                        return ResponseEntity.status(400).body(e.getMessage());
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<ServerResponse> responseEntity(ServerResponse serverResponse) {
        var responseBuilder = ResponseEntity.status(serverResponse.getStatus());
        if (serverResponse.getTokens() != null) {
            addTokenCookies(responseBuilder, serverResponse.getTokens());
        }
        if (serverResponse.getRedirectUrl() != null) {
            return responseBuilder.header("X-Redirect-Location", serverResponse.getRedirectUrl()).build();
        }
        return responseBuilder.body(serverResponse);
    }

    private ResponseEntity.BodyBuilder addTokenCookies(@NonNull ResponseEntity.BodyBuilder responseBuilder, @NonNull ApiTokens tokens) {
        String accessTokenCookie = createAccessTokenCookie(tokens.getAccessToken(), tokens.getAccessTokenExpiresIn()).toString();
        String refreshTokenCookie = createRenewTokenCookie(tokens.getRenewToken(), tokens.getRenewTokenExpiresIn()).toString();
        // Fügen Sie beide "Set-Cookie"-Header in einem Aufruf hinzu, um ein Überschreiben zu verhindern.
        // Spring wird daraus zwei separate "Set-Cookie"-Header in der HTTP-Antwort generieren.
        return responseBuilder.header("Set-Cookie", accessTokenCookie, refreshTokenCookie);
    }

    private ResponseCookie createAccessTokenCookie(String accessToken, Duration maxAge) {
        return createCookie("access_token", accessToken, maxAge);
    }

    private ResponseCookie createRenewTokenCookie(String renewToken, Duration maxAge) {
        return createCookie("refresh_token", renewToken, maxAge);
    }

    private ResponseCookie createCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(maxAge)
                .path("/")
                .build();
    }
}