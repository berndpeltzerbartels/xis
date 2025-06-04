package one.xis.spring;


import lombok.NonNull;
import lombok.Setter;
import one.xis.PathVariable;
import one.xis.server.*;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
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
    public ResponseEntity<ServerResponse> onFormAction(@RequestBody ClientRequest request, Locale locale) {
        request.setLocale(locale);
        var serverResponse = frontendService.processActionRequest(request);
        return ResponseEntity.status(serverResponse.getStatus()).body(serverResponse);
    }

    @Override
    @GetMapping("/xis/auth/{provider}")
    public ResponseEntity<?> auth(HttpRequest request, @PathVariable("provider") String provider) {
        AuthenticationData authData = frontendService.authenticationCallback(provider, request.getURI().getQuery());
        var accessCookie = ResponseCookie.from("access_token", authData.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(Duration.ofSeconds(authData.getAccessTokenExpiresAt() - Instant.now().getEpochSecond()))
                .path("/")
                .build();

        var renewCookie = ResponseCookie.from("refresh_token", authData.getRenewToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(Duration.ofDays(7))  // Oder dynamisch, falls du das auch speicherst
                .path("/")
                .build();

        return ResponseEntity.status(302)
                .header("Location", authData.getUrl()) // z. B. "/dashboard"
                .header("Set-Cookie", accessCookie.toString())
                .header("Set-Cookie", renewCookie.toString())
                .build();
    }

    @Override
    @PostMapping("/xis/token/renew")
    public ResponseEntity<?> renewTokens(@NonNull @RequestHeader("Authentication") String renewToken) {
        var renewTokenResponse = frontendService.processRenewTokenRequest(renewToken.substring("Bearer ".length()));

        var accessCookie = ResponseCookie.from("access_token", renewTokenResponse.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(Duration.ofSeconds(renewTokenResponse.getAccessTokenExpiresAt() - Instant.now().getEpochSecond()))
                .path("/")
                .build();

        var renewCookie = ResponseCookie.from("refresh_token", renewTokenResponse.getRenewToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(Duration.ofDays(7))  // Oder dynamisch, falls du das auch speicherst
                .path("/")
                .build();

        return ResponseEntity.status(201)
                .header("Set-Cookie", accessCookie.toString())
                .header("Set-Cookie", renewCookie.toString())
                .build();
    }

    @Override
    @GetMapping("/xis/page/javascript/{javascriptPath}")
    public String getPageJavascript(@PathVariable("javascriptPath") String javascriptPath) {
        return frontendService.getPageJavascript(javascriptPath);
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
}
