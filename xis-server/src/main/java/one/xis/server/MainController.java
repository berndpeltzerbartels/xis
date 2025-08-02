package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.auth.token.TokenStatus;
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
    public ResponseEntity<?> getPageModel(@RequestBody ClientRequest request, @CookieValue("access_token") String accessToken, @CookieValue("refresh_token") String renewToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setRenewToken(renewToken);
        request.setLocale(httpRequest.getLocale());
        return responseEntity(frontendService.processModelDataRequest(request));
    }

    @Post("/xis/form/model")
    public ResponseEntity<?> getFormModel(@RequestBody ClientRequest request, @CookieValue("access_token") String accessToken, @CookieValue("refresh_token") String renewToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setRenewToken(renewToken);
        request.setLocale(httpRequest.getLocale());
        return responseEntity(frontendService.processFormDataRequest(request));
    }

    @Post("/xis/widget/model")
    public ResponseEntity<?> getWidgetModel(@RequestBody ClientRequest request, @CookieValue("access_token") String accessToken, @CookieValue("refresh_token") String renewToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setRenewToken(renewToken);
        request.setLocale(httpRequest.getLocale());
        return responseEntity(frontendService.processModelDataRequest(request));
    }

    @Post("/xis/page/action")
    public ResponseEntity<?> onPageLinkAction(@RequestBody ClientRequest request, @CookieValue("access_token") String accessToken, @CookieValue("refresh_token") String renewToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setRenewToken(renewToken);
        request.setLocale(httpRequest.getLocale());
        return responseEntity(frontendService.processActionRequest(request));
    }

    @Post("/xis/widget/action")
    public ResponseEntity<?> onWidgetLinkAction(@RequestBody ClientRequest request, @CookieValue("access_token") String accessToken, @CookieValue("refresh_token") String renewToken, HttpRequest httpRequest) {
        request.setAccessToken(accessToken);
        request.setRenewToken(renewToken);
        request.setLocale(httpRequest.getLocale());
        return responseEntity(frontendService.processActionRequest(request));
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
        if (serverResponse.getRedirectUrl() != null) {
            return ResponseEntity.noContent().addHeader("Location", serverResponse.getRedirectUrl());
        }
        var tokenStatus = (TokenStatus) RequestContext.getInstance().getAttribute(TokenStatus.CONTEXT_KEY);
        var entity = ResponseEntity.status(serverResponse.getStatus()).body(serverResponse);
        if (tokenStatus.isRenewed()) {
            entity.addCookie("access_token", tokenStatus.getAccessToken(), tokenStatus.getExpiresIn())
                    .addCookie("renew_token", tokenStatus.getRenewToken(), tokenStatus.getRenewExpiresIn());
        }
        return entity;
    }
}

