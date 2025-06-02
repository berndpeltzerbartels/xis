package one.xis.boot.netty;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.server.*;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
public class NettyController implements FrameworkController<FullHttpResponse, FullHttpRequest, FullHttpResponse> {

    private final FrontendService frontendService;
    private final NettyMapper mapper;


    @Override
    public ClientConfig getComponentConfig() {
        return frontendService.getConfig();
    }

    @Override
    public FullHttpResponse getPageModel(ClientRequest request, Locale locale) {
        request.setLocale(locale);
        try {
            return mapper.toFullHttpResponse(frontendService.processModelDataRequest(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FullHttpResponse getFormModel(ClientRequest request, Locale locale) {
        request.setLocale(locale);
        try {
            return mapper.toFullHttpResponse(frontendService.processFormDataRequest(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FullHttpResponse getWidgetModel(ClientRequest request, Locale locale) {
        request.setLocale(locale);
        try {
            return mapper.toFullHttpResponse(frontendService.processModelDataRequest(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FullHttpResponse onPageLinkAction(ClientRequest request, Locale locale) {
        request.setLocale(locale);
        try {
            return mapper.toFullHttpResponse(frontendService.processActionRequest(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FullHttpResponse onWidgetLinkAction(ClientRequest request, Locale locale) {
        request.setLocale(locale);
        try {
            return mapper.toFullHttpResponse(frontendService.processActionRequest(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FullHttpResponse onFormAction(ClientRequest request, Locale locale) {
        request.setLocale(locale);
        try {
            return mapper.toFullHttpResponse(frontendService.processActionRequest(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FullHttpResponse auth(FullHttpRequest request, String provider) {
        String query = new QueryStringDecoder(request.uri()).rawQuery();
        AuthenticationData authData = frontendService.authenticationCallback(provider, query);
        return mapper.toRedirectWithCookies(authData.getUrl(), authData);
    }

    @Override
    public RenewTokenResponse renewToken(RenewTokenRequest request) {
        return frontendService.processRenewTokenRequest(request);
    }

    @Override
    public String getPageJavascript(String path) {
        return frontendService.getPageJavascript(path);
    }

    @Override
    public String getPage(String id) {
        return frontendService.getPage(id);
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


}
