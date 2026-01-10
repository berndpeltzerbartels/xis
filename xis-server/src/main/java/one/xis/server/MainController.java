package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.auth.token.TokenStatus;
import one.xis.http.*;
import one.xis.resource.Resource;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

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
    private <T> ResponseEntity<T> handleResourceResponse(Resource resource, String ifModifiedSinceStr, Function<Resource, T> contentExtractor) {
        long lastModified = resource.getLastModified();
        String cacheControl = "no-cache";
        Instant lastModifiedResource = Instant.ofEpochMilli(lastModified).truncatedTo(ChronoUnit.SECONDS);
        if (ifModifiedSinceStr != null && !ifModifiedSinceStr.isEmpty()) {
            Instant ifModifiedSince = parseHttpDate(ifModifiedSinceStr);
            if (ifModifiedSince != null && lastModifiedResource != null && !lastModifiedResource.isAfter(ifModifiedSince)) {
                return (ResponseEntity<T>) ResponseEntity.status(304)
                        .addHeader("Last-Modified", formatHttpDate(lastModifiedResource))
                        .addHeader("Cache-Control", cacheControl);
            }
        }
        T body = contentExtractor.apply(resource);
        var response = ResponseEntity.ok(body)
                .addHeader("Cache-Control", cacheControl);
        if (lastModifiedResource != null) {
            response.addHeader("Last-Modified", formatHttpDate(lastModifiedResource));
        }
        return response;
    }

    @Get("/xis/page/head")
    public ResponseEntity<String> getPageHead(@UrlParameter("pageId") String pageId, @RequestHeader("If-Modified-Since") String ifModifiedSince) {
        Resource resource = frontendService.getPageHead(pageId);
        return handleResourceResponse(resource, ifModifiedSince, Resource::getContent);
    }

    @Get("/xis/page/body")
    public ResponseEntity<String> getPageBody(@UrlParameter("pageId") String pageId, @RequestHeader("If-Modified-Since") String ifModifiedSince) {
        Resource resource = frontendService.getPageBody(pageId);
        return handleResourceResponse(resource, ifModifiedSince, Resource::getContent);
    }

    @Get("/xis/page/body-attributes")
    public ResponseEntity<?> getBodyAttributes(@UrlParameter("pageId") String pageId, @RequestHeader("If-Modified-Since") String ifModifiedSince) {
        Resource resource = frontendService.getBodyAttributes(pageId);
        return handleResourceResponse(resource, ifModifiedSince, Resource::getContent);
    }

    @Get("/xis/widget/html")
    public ResponseEntity<String> getWidgetHtml(@UrlParameter("widgetId") String widgetId, @RequestHeader("If-Modified-Since") String ifModifiedSince) {
        Resource resource = frontendService.getWidgetHtml(widgetId);
        return handleResourceResponse(resource, ifModifiedSince, Resource::getContent);
    }

    @Get("/xis/include/html")
    public ResponseEntity<String> getIncludeHtml(@UrlParameter("key") String key, @RequestHeader("If-Modified-Since") String ifModifiedSince) {
        Resource resource = frontendService.getIncludeHtml(key);
        return handleResourceResponse(resource, ifModifiedSince, Resource::getContent);
    }

    @Get("/bundle.min.js")
    @ResponseHeader(name = "SourceMap", value = "/bundle.min.js.map")
    public ResponseEntity<String> getBundleJs(@RequestHeader("If-Modified-Since") String ifModifiedSince) {
        Resource resource = frontendService.getBundleJs();
        return handleResourceResponse(resource, ifModifiedSince, Resource::getContent);
    }


    @Get("/bundle.min.js.map")
    @ResponseHeader(name = "Content-Type", value = "application/json")
    public ResponseEntity<String> getBundleJsMap(@RequestHeader("If-Modified-Since") String ifModifiedSince) {
        Resource resource = frontendService.getBundleJsMap();
        return handleResourceResponse(resource, ifModifiedSince, Resource::getContent);
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

    private Instant parseHttpDate(String httpDate) {
        try {
            return ZonedDateTime.parse(httpDate, RFC_1123_DATE_TIME).toInstant();
        } catch (Exception e) {
            return null;
        }
    }

    private String formatHttpDate(Instant instant) {
        return RFC_1123_DATE_TIME.format(instant.atZone(java.time.ZoneId.of("GMT")));
    }
}
