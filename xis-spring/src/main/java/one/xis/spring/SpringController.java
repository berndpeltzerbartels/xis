package one.xis.spring;


import lombok.NonNull;
import lombok.Setter;
import one.xis.PathVariable;
import one.xis.security.AuthenticationException;
import one.xis.server.*;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;

@Setter
@RestController
@RequestMapping
class SpringController implements FrameworkController<ResponseEntity<ServerResponse>, HttpRequest, ResponseEntity<?>> {

    private FrontendService frontendService;

    @Override
    @GetMapping("/xis/config")
    public ClientConfig getComponentConfig(HttpServletRequest request) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return frontendService.getConfig();
    }

    @Override
    @PostMapping("/xis/page/model")
    public ResponseEntity<ServerResponse> getPageModel(@RequestBody ClientRequest request,
                                                       @RequestHeader(value = "Authentication", required = false) String authenticationHeader,
                                                       Locale locale) {
        addTokenToRequest(request, authenticationHeader);
        request.setLocale(locale);
        var serverResponse = frontendService.processModelDataRequest(request);
        return responseEntity(serverResponse);
    }

    @Override
    @PostMapping("/xis/form/model")
    public ResponseEntity<ServerResponse> getFormModel(@RequestBody ClientRequest request,
                                                       @RequestHeader(value = "Authentication", required = false) String authenticationHeader,
                                                       Locale locale) {
        addTokenToRequest(request, authenticationHeader);
        request.setLocale(locale);
        var serverResponse = frontendService.processFormDataRequest(request);
        return responseEntity(serverResponse);
    }

    @Override
    @PostMapping("/xis/widget/model")
    public ResponseEntity<ServerResponse> getWidgetModel(@RequestBody ClientRequest request,
                                                         @RequestHeader(value = "Authentication", required = false) String authenticationHeader,
                                                         Locale locale) {
        addTokenToRequest(request, authenticationHeader);
        request.setLocale(locale);
        var serverResponse = frontendService.processModelDataRequest(request);
        return responseEntity(serverResponse);
    }

    @Override
    @PostMapping("/xis/page/action")
    public ResponseEntity<ServerResponse> onPageLinkAction(@RequestBody ClientRequest request,
                                                           @RequestHeader(value = "Authentication", required = false) String authenticationHeader,
                                                           Locale locale) {
        addTokenToRequest(request, authenticationHeader);
        request.setLocale(locale);
        var serverResponse = frontendService.processActionRequest(request);
        return responseEntity(serverResponse);
    }

    @Override
    @PostMapping("/xis/widget/action")
    public ResponseEntity<ServerResponse> onWidgetLinkAction(@RequestBody ClientRequest request,
                                                             @RequestHeader(value = "Authentication", required = false) String authenticationHeader,
                                                             Locale locale) {
        addTokenToRequest(request, authenticationHeader);
        request.setLocale(locale);
        var serverResponse = frontendService.processActionRequest(request);
        return responseEntity(serverResponse);
    }

    @Override
    @PostMapping("/xis/form/action")
    public ResponseEntity<?> onFormAction(@RequestBody ClientRequest request,
                                          @RequestHeader(value = "Authentication", required = false) String authenticationHeader,
                                          Locale locale) {
        addTokenToRequest(request, authenticationHeader);
        request.setLocale(locale);
        return responseEntity(frontendService.processActionRequest(request));
    }

    @Override
    @PostMapping(value = "/xis/idp/tokens", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> idpGetTokens(@RequestParam("code") String code,
                                          @RequestParam("state") String state) {
        BearerTokens tokens;
        try {
            tokens = frontendService.localTokenProviderGetTokens(code, state);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Authentication failed");
        }
        return addTokenCookies(ResponseEntity.status(201), tokens).build();
    }

    @Override
    @GetMapping("/xis/auth/{provider}")
    public ResponseEntity<?> authenticationCallback(HttpRequest request,
                                                    @PathVariable("provider") String provider) {
        AuthenticationData authData = frontendService.authenticationCallback(provider, request.getURI().getQuery());
        return addTokenCookies(ResponseEntity.status(302).header("Location", authData.getUrl()), authData.getApiTokens()).build();
    }

    @Override
    @PostMapping("/xis/token/renew")
    public ResponseEntity<?> renewApiTokens(@NonNull @RequestHeader("Authentication") String renewToken) {
        var renewTokenResponse = frontendService.processRenewApiTokenRequest(renewToken.substring("Bearer ".length()));
        return addTokenCookies(ResponseEntity.status(201), renewTokenResponse).build();
    }

    @Override
    @GetMapping("/xis/page")
    public String getPage(@RequestHeader("uri") String id) {
        return frontendService.getPage(id);
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

    private void addTokenToRequest(ClientRequest request, String authenticationHeader) {
        request.setAccessToken(extractAccessToken(authenticationHeader));
    }

    private String extractAccessToken(String authenticationHeader) {
        if (authenticationHeader != null && authenticationHeader.startsWith("Bearer ")) {
            return authenticationHeader.substring("Bearer ".length());
        }
        return null;
    }


    private ResponseEntity<ServerResponse> responseEntity(ServerResponse serverResponse) {
        var responseBuilder = ResponseEntity.status(serverResponse.getStatus());
        if (serverResponse.getTokens() != null) {
            addTokenCookies(responseBuilder, serverResponse.getTokens());
        }
        if (serverResponse.getRedirectUrl() != null) {
            return responseBuilder.header("Location", serverResponse.getRedirectUrl()).build();
        }
        return responseBuilder.body(serverResponse);
    }

    private ResponseEntity.BodyBuilder addTokenCookies(@NonNull ResponseEntity.BodyBuilder responseBuilder, @NonNull ApiTokens tokens) {
        responseBuilder.header("Set-Cookie", createAccessTokenCookie(tokens.getAccessToken(), tokens.getAccessTokenExpiresIn()).toString());
        responseBuilder.header("Set-Cookie", createRenewTokenCookie(tokens.getRenewToken(), tokens.getRenewTokenExpiresIn()).toString());
        return responseBuilder;
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
                .sameSite("Strict")
                .maxAge(maxAge)
                .path("/")
                .build();
    }
}
