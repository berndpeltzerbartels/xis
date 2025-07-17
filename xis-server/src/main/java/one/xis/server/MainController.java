package one.xis.server;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.auth.AuthenticationException;
import one.xis.auth.token.ApiTokens;
import one.xis.http.*;
import org.tinylog.Logger;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final FrontendService frontendService;
    private final LocalUrlHolder localUrlHolder;

    @Get("/xis/config")
    public ResponseEntity<?> getComponentConfig() {
        return ResponseEntity.ok(frontendService.getConfig());
    }

    @Post("/xis/page/model")
    public ResponseEntity<?> getPageModel(@RequestBody ClientRequest request, @CookieValue("access_token") String accessToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setLocale(httpRequest.getLocale());
        try {
            return responseEntity(frontendService.processModelDataRequest(request)); // TODO ControllerAdvice
        } catch (AuthenticationException e) {
            Logger.error("Authentication error: {}", e.getMessage());
            return authenticationErrorResponse(request);
        }
    }

    @Post("/xis/form/model")
    public ResponseEntity<?> getFormModel(@RequestBody ClientRequest request, @BearerToken(optional = true) String accessToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setLocale(httpRequest.getLocale());
        try {
            return responseEntity(frontendService.processFormDataRequest(request));
        } catch (AuthenticationException e) {
            Logger.error("Authentication error: {}", e.getMessage());
            return authenticationErrorResponse(request);
        }
    }

    @Post("/xis/widget/model")
    public ResponseEntity<?> getWidgetModel(@RequestBody ClientRequest request, @BearerToken(optional = true) String accessToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setLocale(httpRequest.getLocale());
        try {
            return responseEntity(frontendService.processModelDataRequest(request));
        } catch (AuthenticationException e) {
            Logger.error("Authentication error: {}", e.getMessage());
            return authenticationErrorResponse(request);
        }
    }

    @Post("/xis/page/action")
    public ResponseEntity<?> onPageLinkAction(@RequestBody ClientRequest request, @BearerToken(optional = true) String accessToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setLocale(httpRequest.getLocale());
        try {
            return responseEntity(frontendService.processActionRequest(request));
        } catch (AuthenticationException e) {
            Logger.error("Authentication error: {}", e.getMessage());
            return authenticationErrorResponse(request);
        }
    }

    @Post("/xis/widget/action")
    public ResponseEntity<?> onWidgetLinkAction(@RequestBody ClientRequest request, @BearerToken(optional = true) String accessToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setLocale(httpRequest.getLocale());
        try {
            return responseEntity(frontendService.processActionRequest(request));
        } catch (AuthenticationException e) {
            Logger.error("Authentication error: {}", e.getMessage());
            return authenticationErrorResponse(request);
        }
    }

    @Post("/xis/form/action")
    public ResponseEntity<?> onFormAction(@RequestBody ClientRequest request, @BearerToken(optional = true) String accessToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setLocale(httpRequest.getLocale());
        try {
            return responseEntity(frontendService.processActionRequest(request));
        } catch (AuthenticationException e) {
            Logger.error("Authentication error: {}", e.getMessage());
            return authenticationErrorResponse(request);
        }
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

    private ResponseEntity<?> authenticationErrorResponse(ClientRequest request) {
        ServerResponse serverResponse = new ServerResponse();
        serverResponse.setStatus(401);
        return responseEntity(serverResponse);
    }

    private ResponseEntity<?> responseEntity(ServerResponse serverResponse) {
        ResponseEntity<?> entity;
        if (serverResponse.getRedirectUrl() != null) {
            entity = ResponseEntity.redirect(serverResponse.getRedirectUrl());
        } else {
            entity = ResponseEntity.status(serverResponse.getStatus()).body(serverResponse);
        }
        if (serverResponse.getTokens() != null) {
            addTokenHeaders(entity, serverResponse.getTokens());
        }
        return entity;
    }


    private void addTokenHeaders(@NonNull ResponseEntity<?> entity, ApiTokens tokens) {
        if (localUrlHolder.isSecure()) {
            entity.addSecureCookie("access_token", tokens.getAccessToken(), tokens.getAccessTokenExpiresIn());
            entity.addSecureCookie("refresh_token", tokens.getRenewToken(), tokens.getRenewTokenExpiresIn());
        } else {
            entity.addCookie("refresh_token", tokens.getRenewToken(), tokens.getRenewTokenExpiresIn());
            entity.addCookie("refresh_token", tokens.getRenewToken(), tokens.getRenewTokenExpiresIn());
        }
    }
}

