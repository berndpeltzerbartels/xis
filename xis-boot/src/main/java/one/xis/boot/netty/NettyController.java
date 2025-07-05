package one.xis.boot.netty;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.server.ClientConfig;
import one.xis.server.ClientRequest;
import one.xis.server.FrameworkController;
import one.xis.server.FrontendService;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
public class NettyController implements FrameworkController<FullHttpResponse, FullHttpRequest, FullHttpResponse> {

    private final FrontendService frontendService;
    private final NettyMapper mapper;

    @Override
    public ClientConfig getComponentConfig(FullHttpRequest request) {
        String host = request.headers().get("Host");
        String scheme;
        if (request.headers().contains("X-Forwarded-Proto")) {
            // Priorität 1: Header vom Reverse-Proxy
            scheme = request.headers().get("X-Forwarded-Proto");
        } else if (request.headers().contains("X-Internal-Scheme")) {
            // Priorität 2: Intern gesetzter Header für direkte Verbindungen
            scheme = request.headers().get("X-Internal-Scheme");
        } else {
            // Fallback, falls der SchemeInjectorHandler nicht konfiguriert ist
            scheme = "http";
        }
        String baseUrl = scheme + "://" + host;
        frontendService.setLocalUrl(baseUrl);
        return frontendService.getConfig();
    }

    @Override
    public FullHttpResponse getPageModel(ClientRequest request, String authenticationHeader, Locale locale) {
        request.setLocale(locale);
        request.setAccessToken(extractToken(authenticationHeader));
        try {
            return mapper.toFullHttpResponse(frontendService.processModelDataRequest(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FullHttpResponse getFormModel(ClientRequest request, String authenticationHeader, Locale locale) {
        request.setLocale(locale);
        request.setAccessToken(extractToken(authenticationHeader));
        try {
            return mapper.toFullHttpResponse(frontendService.processFormDataRequest(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FullHttpResponse getWidgetModel(ClientRequest request, String authenticationHeader, Locale locale) {
        request.setLocale(locale);
        request.setAccessToken(extractToken(authenticationHeader));
        try {
            return mapper.toFullHttpResponse(frontendService.processModelDataRequest(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FullHttpResponse onPageLinkAction(ClientRequest request, String authenticationHeader, Locale locale) {
        request.setLocale(locale);
        request.setAccessToken(extractToken(authenticationHeader));
        try {
            return mapper.toFullHttpResponse(frontendService.processActionRequest(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FullHttpResponse onWidgetLinkAction(ClientRequest request, String authenticationHeader, Locale locale) {
        request.setLocale(locale);
        request.setAccessToken(extractToken(authenticationHeader));
        try {
            return mapper.toFullHttpResponse(frontendService.processActionRequest(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FullHttpResponse onFormAction(ClientRequest request, String authenticationHeader, Locale locale) {
        request.setLocale(locale);
        request.setAccessToken(extractToken(authenticationHeader));
        try {
            return mapper.toFullHttpResponse(frontendService.processActionRequest(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPageHead(String id) {
        return frontendService.getPageHead(id);
    }

    @Override
    public String getPageBody(String id) {
        return frontendService.getPageBody(id);
    }

    @Override
    public Map<String, String> getBodyAttributes(String id) {
        return frontendService.getBodyAttributes(id);
    }

    @Override
    public String getWidgetHtml(String id) {
        return frontendService.getWidgetHtml(id);
    }

    @Override
    public String getAppJs() {
        return frontendService.getAppJs();
    }

    @Override
    public String getClassesJs() {
        return frontendService.getClassesJs();
    }

    @Override
    public String getMainJs() {
        return frontendService.getMainJs();
    }

    @Override
    public String getFunctionsJs() {
        return frontendService.getFunctionsJs();
    }

    @Override
    public String getBundleJs() {
        return frontendService.getBundleJs();
    }

    private String extractToken(String authenticationHeader) {
        if (authenticationHeader != null && authenticationHeader.startsWith("Bearer ")) {
            return authenticationHeader.substring("Bearer ".length());
        }
        return null;
    }
}
