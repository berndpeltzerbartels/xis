package one.xis.spring;


import lombok.NonNull;
import lombok.Setter;
import one.xis.PathVariable;
import one.xis.security.AuthenticationException;
import one.xis.security.InvalidCredentialsException;
import one.xis.security.Login;
import one.xis.server.*;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ClientConfig getComponentConfig() {
        return frontendService.getConfig();
    }

    @Override
    @PostMapping("/xis/page/model")
    public ResponseEntity<ServerResponse> getPageModel(@RequestBody ClientRequest request, Locale locale) {
        request.setLocale(locale);
        var serverResponse = frontendService.processModelDataRequest(request);
        return ResponseEntity.status(serverResponse.getStatus()).body(serverResponse);
    }

    @Override
    @PostMapping("/xis/form/model")
    public ResponseEntity<ServerResponse> getFormModel(@RequestBody ClientRequest request, Locale locale) {
        request.setLocale(locale);
        var serverResponse = frontendService.processFormDataRequest(request);
        return ResponseEntity.status(serverResponse.getStatus()).body(serverResponse);
    }

    @Override
    @PostMapping("/xis/widget/model")
    public ResponseEntity<ServerResponse> getWidgetModel(@RequestBody ClientRequest request, Locale locale) {
        request.setLocale(locale);
        var serverResponse = frontendService.processModelDataRequest(request);
        return ResponseEntity.status(serverResponse.getStatus()).body(serverResponse);
    }

    @Override
    @PostMapping("/xis/page/action")
    public ResponseEntity<ServerResponse> onPageLinkAction(@RequestBody ClientRequest request, Locale locale) {
        request.setLocale(locale);
        var serverResponse = frontendService.processActionRequest(request);
        return ResponseEntity.status(serverResponse.getStatus()).body(serverResponse);
    }

    @Override
    @PostMapping("/xis/widget/action")
    public ResponseEntity<ServerResponse> onWidgetLinkAction(@RequestBody ClientRequest request, Locale locale) {
        request.setLocale(locale);
        var serverResponse = frontendService.processActionRequest(request);
        return ResponseEntity.status(serverResponse.getStatus()).body(serverResponse);
    }

    @Override
    @PostMapping("/xis/form/action")
    public ResponseEntity<?> onFormAction(@RequestBody ClientRequest request, Locale locale) {
        request.setLocale(locale);
        if (request.getAction().equals("login")) {
            try {
                var tokens = frontendService.processLoginRequest(request);
                var accessCookie = createAccessTokenCookie(tokens.getAccessToken(), tokens.getAccessTokenExpiresIn());
                var renewCookie = createRenewTokenCookie(tokens.getRenewToken(), tokens.getRenewTokenExpiresIn());
                return ResponseEntity.status(201)
                        .header("Set-Cookie", accessCookie.toString())
                        .header("Set-Cookie", renewCookie.toString())
                        .build();
            } catch (InvalidCredentialsException e) {
                return ResponseEntity.status(401).body("Invalid credentials");
            }
        } else {
            var serverResponse = frontendService.processActionRequest(request);
            return ResponseEntity.status(serverResponse.getStatus()).body(serverResponse);
        }
    }

    @Override
    @PostMapping("/xis/token-provider/login")
    public ResponseEntity<?> localTokenProviderLogin(Login login) {
        try {
            var code = frontendService.localTokenProviderLogin(login);
            var state = login.getState();
            return ResponseEntity.status(301)
                    .header("Location", "/xis/auth/local?code=" + code + "&state=" + state) // Redirect to the auth endpoint
                    .build();
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @Override
    @PostMapping(value = "/xis/token-provider/tokens", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> localTokenProviderGetTokens(@RequestParam("code") String code,
                                                         @RequestParam("state") String state) {
        BearerTokens tokens;
        try {
            tokens = frontendService.localTokenProviderGetTokens(code, state);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Authentication failed");
        }
        var accessCookie = createAccessTokenCookie(tokens.getAccessToken(), tokens.getAccessTokenExpiresIn());
        var renewCookie = createRenewTokenCookie(tokens.getRenewToken(), tokens.getRenewTokenExpiresIn());
        return ResponseEntity.status(201)
                .header("Set-Cookie", accessCookie.toString())
                .header("Set-Cookie", renewCookie.toString())
                .build();
    }

    @Override
    @GetMapping("/xis/auth/{provider}")
    public ResponseEntity<?> authenticationCallback(HttpRequest request, @PathVariable("provider") String provider) {
        AuthenticationData authData = frontendService.authenticationCallback(provider, request.getURI().getQuery());
        var accessCookie = createAccessTokenCookie(authData.getApiTokens().getAccessToken(), authData.getApiTokens().getAccessTokenExpiresIn());
        var renewCookie = createRenewTokenCookie(authData.getApiTokens().getRenewToken(), authData.getApiTokens().getRenewTokenExpiresIn());
        return ResponseEntity.status(302)
                .header("Location", authData.getUrl()) // z. B. "/dashboard"
                .header("Set-Cookie", accessCookie.toString())
                .header("Set-Cookie", renewCookie.toString())
                .build();
    }

    @Override
    @PostMapping("/xis/token/renew")
    public ResponseEntity<?> renewApiTokens(@NonNull @RequestHeader("Authentication") String renewToken) {
        var renewTokenResponse = frontendService.processRenewApiTokenRequest(renewToken.substring("Bearer ".length()));
        var accessCookie = createAccessTokenCookie(renewTokenResponse.getAccessToken(), renewTokenResponse.getAccessTokenExpiresIn());
        var renewCookie = createRenewTokenCookie(renewTokenResponse.getRenewToken(), renewTokenResponse.getRenewTokenExpiresIn());
        return ResponseEntity.status(201)
                .header("Set-Cookie", accessCookie.toString())
                .header("Set-Cookie", renewCookie.toString())
                .build();
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
