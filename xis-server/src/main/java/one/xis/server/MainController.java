package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.auth.token.TokenStatus;
import one.xis.http.*;
import one.xis.resource.Resource;

@Controller
@PublicResources("/public")
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

    @SuppressWarnings("unchecked")
    private <T> ResponseEntity<T> handleResourceResponse(Resource resource, String ifModifiedSince, java.util.function.Function<Resource, T> contentExtractor) {
        long lastModified = resource.getLastModified();
        String cacheControl = "private, max-age=3600";
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            long ifModifiedSinceEpoch = parseHttpDate(ifModifiedSince);
            if (lastModified > 0 && lastModified <= ifModifiedSinceEpoch) {
                return (ResponseEntity<T>) ResponseEntity.status(304)
                        .addHeader("Last-Modified", formatHttpDate(lastModified))
                        .addHeader("Cache-Control", cacheControl);
            }
        }
        T body = contentExtractor.apply(resource);
        return ResponseEntity.ok(body)
                .addHeader("Last-Modified", formatHttpDate(lastModified))
                .addHeader("Cache-Control", cacheControl);
    }

    @Get("/xis/page/head")
    public ResponseEntity<String> getPageHead(@UrlParameter("pageId") String pageId, @Header("If-Modified-Since") String ifModifiedSince) {
        Resource resource = frontendService.getPageHead(pageId);
        return handleResourceResponse(resource, ifModifiedSince, Resource::getContent);
    }

    @Get("/xis/page/body")
    public ResponseEntity<String> getPageBody(@UrlParameter("pageId") String pageId, @Header("If-Modified-Since") String ifModifiedSince) {
        Resource resource = frontendService.getPageBody(pageId);
        return handleResourceResponse(resource, ifModifiedSince, Resource::getContent);
    }

    @Get("/xis/page/body-attributes")
    public ResponseEntity<?> getBodyAttributes(@UrlParameter("pageId") String pageId, @Header("If-Modified-Since") String ifModifiedSince) {
        Resource resource = frontendService.getBodyAttributes(pageId);
        return handleResourceResponse(resource, ifModifiedSince, Resource::getContent);
    }

    @Get("/xis/widget/html}")
    public ResponseEntity<String> getWidgetHtml(@UrlParameter("widgetId") String widgetId, @Header("If-Modified-Since") String ifModifiedSince) {
        Resource resource = frontendService.getWidgetHtml(widgetId);
        return handleResourceResponse(resource, ifModifiedSince, Resource::getContent);
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


    @Get("/bundle.min.js.map")
    public ResponseEntity<String> getBundleJsMap() {
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

    private long parseHttpDate(String httpDate) {
        try {
            return java.time.ZonedDateTime.parse(httpDate, java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toInstant().toEpochMilli();
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatHttpDate(long epochMilli) {
        return java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
                .format(java.time.Instant.ofEpochMilli(epochMilli).atZone(java.time.ZoneId.of("GMT")));
    }
}
