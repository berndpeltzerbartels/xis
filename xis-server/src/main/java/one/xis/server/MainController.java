package one.xis.server;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.auth.token.ApiTokens;
import one.xis.http.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final FrontendService frontendService;

    @Get("/xis/config")
    public ResponseEntity<?> getComponentConfig() {
        return ResponseEntity.ok(frontendService.getConfig());
    }

    @Post("/xis/page/model")
    public ResponseEntity<?> getPageModel(@RequestBody ClientRequest request, @CookieValue("access_token") String accessToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setLocale(httpRequest.getLocale());
        ServerResponse serverResponse = frontendService.processModelDataRequest(request);
        return responseEntity(serverResponse);
    }

    @Post("/xis/form/model")
    public ResponseEntity<?> getFormModel(@RequestBody ClientRequest request, @CookieValue("access_token") String accessToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setLocale(httpRequest.getLocale());
        ServerResponse serverResponse = frontendService.processFormDataRequest(request);
        return responseEntity(serverResponse);
    }

    @Post("/xis/widget/model")
    public ResponseEntity<?> getWidgetModel(@RequestBody ClientRequest request, @CookieValue("access_token") String accessToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setLocale(httpRequest.getLocale());
        ServerResponse serverResponse = frontendService.processModelDataRequest(request);
        return responseEntity(serverResponse);
    }

    @Post("/xis/page/action")
    public ResponseEntity<?> onPageLinkAction(@RequestBody ClientRequest request, @CookieValue("access_token") String accessToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setLocale(httpRequest.getLocale());
        ServerResponse serverResponse = frontendService.processActionRequest(request);
        return responseEntity(serverResponse);
    }

    @Post("/xis/widget/action")
    public ResponseEntity<?> onWidgetLinkAction(@RequestBody ClientRequest request, @CookieValue("access_token") String accessToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setLocale(httpRequest.getLocale());
        ServerResponse serverResponse = frontendService.processActionRequest(request);
        return responseEntity(serverResponse);
    }

    @Post("/xis/form/action")
    public ResponseEntity<?> onFormAction(@RequestBody ClientRequest request, @CookieValue("access_token") String accessToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setLocale(httpRequest.getLocale());
        return responseEntity(frontendService.processActionRequest(request));
    }

    @Get("/xis/page/head")
    public ResponseEntity<String> getPageHead(@Header("uri") String id) {
        return ResponseEntity.ok(frontendService.getPageHead(id));
    }

    @Get("/xis/page/body")
    public ResponseEntity<String> getPageBody(@Header("uri") String id) {
        return ResponseEntity.ok(frontendService.getPageBody(id));
    }

    @Get("/xis/page/body-attributes")
    public ResponseEntity<Map<String, String>> getBodyAttributes(@Header("uri") String id) {
        return ResponseEntity.ok(frontendService.getBodyAttributes(id));
    }

    @Get("/xis/widget/html")
    public ResponseEntity<String> getWidgetHtml(@Header("uri") String id) {
        return ResponseEntity.ok(frontendService.getWidgetHtml(id));
    }

    @Get("/app.js")
    public ResponseEntity<String> getAppJs() {
        return ResponseEntity.ok(frontendService.getAppJs());
    }

    @Get("/classes.js")
    public ResponseEntity<String> getClassesJs() {
        return ResponseEntity.ok(frontendService.getClassesJs());
    }

    @Get("/main.js")
    public ResponseEntity<String> getMainJs() {
        return ResponseEntity.ok(frontendService.getMainJs());
    }

    @Get("/functions.js")
    public ResponseEntity<String> getFunctionsJs() {
        return ResponseEntity.ok(frontendService.getFunctionsJs());
    }

    @Get("/bundle.min.js")
    public ResponseEntity<String> getBundleJs() {
        return ResponseEntity.ok(frontendService.getBundleJs());
    }

    private ResponseEntity<?> responseEntity(ServerResponse serverResponse) {
        ResponseEntity<?> entity = ResponseEntity.status(serverResponse.getStatus(), serverResponse);
        if (serverResponse.getTokens() != null) {
            addTokenCookies(entity, serverResponse.getTokens());
        }
        if (serverResponse.getRedirectUrl() != null) {
            return entity.addHeader("X-Redirect-Location", serverResponse.getRedirectUrl());
        }
        return entity;
    }

    private void addTokenCookies(@NonNull ResponseEntity<?> entity, @NonNull ApiTokens tokens) {
        entity.addSecureCookie("access_token", tokens.getAccessToken(), tokens.getAccessTokenExpiresIn());
        entity.addSecureCookie("refresh_token", tokens.getRenewToken(), tokens.getRenewTokenExpiresIn());
    }

}

    /*

    @Get("/.well-known/openid-configuration")
    public ResponseEntity<String> getOpenIdConfiguration() {
        return appContext.getOptionalSingleton(IDPFrontendService.class)
                .map(idp -> ResponseEntity.ok(idp.getOpenIdConfigJson()))
                .orElse(ResponseEntity.notFound().build());
    }

    @Get("/.well-known/jwks.json")
    public ResponseEntity<String> getIdpPublicKey() {
        return appContext.getOptionalSingleton(IDPFrontendService.class)
                .map(idp -> ResponseEntity.ok(idp.getPublicKey()))
                .orElse(ResponseEntity.notFound().build());
    }

    @Get("/auth/callback/{idpId}")
    public ResponseEntity<?> authenticationCallback(@RequestParam("code") String code, @RequestParam("state") String state) {
        TokensAndUrl tokensAndUrl = frontendService.authenticationCallback(code, state);
        ResponseEntity.Builder<?> builder = ResponseEntity.status(302).header("Location", tokensAndUrl.getUrl());
        addTokenCookies(builder, tokensAndUrl.getApiTokens());
        return builder.build();
    }

    @Post(value = "/xis/auth/tokens", consumes = "application/x-www-form-urlencoded", produces = "application/json")
    public ResponseEntity<?> getIdpTokens(@RequestBody String body) {
        return appContext.getOptionalSingleton(IDPFrontendService.class)
                .map(idpFrontendService -> {
                    try {
                        var idpResponse = idpFrontendService.provideTokens(body);
                        var builder = ResponseEntity.ok(idpResponse.toOAuth2Response());
                        addTokenCookies(builder, idpResponse.getApiTokens());
                        return builder.build();
                    } catch (Exception e) {
                        return ResponseEntity.status(400).body(e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private ResponseEntity<?> responseEntity(ServerResponse serverResponse) {
        ResponseEntity.Builder<ServerResponse> builder = ResponseEntity.status(serverResponse.getStatus());
        if (serverResponse.getTokens() != null) {
            addTokenCookies(builder, serverResponse.getTokens());
        }
        if (serverResponse.getRedirectUrl() != null) {
            return builder.header("X-Redirect-Location", serverResponse.getRedirectUrl()).build();
        }
        return builder.body(serverResponse);
    }

    private void addTokenCookies(@NonNull ResponseEntity.Builder<?> builder, @NonNull ApiTokens tokens) {
        builder.cookie(createCookie("access_token", tokens.getAccessToken(), tokens.getAccessTokenExpiresIn()));
        builder.cookie(createCookie("refresh_token", tokens.getRenewToken(), tokens.getRenewTokenExpiresIn()));
    }

    private Cookie createCookie(String name, String value, Duration maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setSameSite("Lax");
        cookie.setMaxAge(maxAge.getSeconds());
        cookie.setPath("/");
        return cookie;
    }

     */
