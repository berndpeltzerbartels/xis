package one.xis.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import one.xis.security.AuthenticationException;
import one.xis.security.InvalidCredentialsException;
import one.xis.security.Login;
import one.xis.server.*;
import one.xis.validation.ValidatorMessages;

import java.util.Locale;
import java.util.Map;

import static one.xis.context.BackendBridgeVerboseRunner.run;

@XISComponent
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class BackendBridge implements ResourcePathProvider {

    private final FrontendService frontendService;
    private final ObjectMapper objectMapper;
    private final AppContext appContext;

    public BackendBridgeResponse getComponentConfig(String uri, Map<String, String> headers) {
        return toBridgeResponse(run(frontendService::getConfig));
    }

    public BackendBridgeResponse getPageModel(String uri, String requestJson, Map<String, String> headers) {
        return toBridgeResponse(run(frontendService::processModelDataRequest, request(requestJson, headers)));
    }

    public BackendBridgeResponse getFormModel(String uri, String requestJson, Map<String, String> headers) {
        return toBridgeResponse(run(frontendService::processFormDataRequest, request(requestJson, headers)));
    }

    public BackendBridgeResponse getWidgetModel(String uri, String requestJson, Map<String, String> headers) {
        return toBridgeResponse(run(frontendService::processModelDataRequest, request(requestJson, headers)));
    }

    public BackendBridgeResponse onPageLinkAction(String uri, String requestJson, Map<String, String> headers) {
        return toBridgeResponse(run(frontendService::processActionRequest, request(requestJson, headers)));
    }

    public BackendBridgeResponse onWidgetLinkAction(String uri, String requestJson, Map<String, String> headers) {
        return toBridgeResponse(run(frontendService::processActionRequest, request(requestJson, headers)));
    }

    public BackendBridgeResponse onFormAction(String uri, String requestJson, Map<String, String> headers) {
        return toBridgeResponse(run(frontendService::processActionRequest, request(requestJson, headers)));
    }

    public BackendBridgeResponse getPage(String uri, Map<String, String> headers) {
        return stringToBridgeResponse(frontendService.getPage(headers.get("uri")));
    }

    public BackendBridgeResponse getPageHead(String uri, Map<String, String> headers) {
        return stringToBridgeResponse(frontendService.getPageHead(headers.get("uri")));
    }

    public BackendBridgeResponse getPageBody(String uri, Map<String, String> headers) {
        return stringToBridgeResponse(frontendService.getPageBody(headers.get("uri")));
    }

    public BackendBridgeResponse getBodyAttributes(String uri, Map<String, String> headers) {
        return toBridgeResponse(frontendService.getBodyAttributes(headers.get("uri")));
    }

    public BackendBridgeResponse getWidgetHtml(String uri, Map<String, String> headers) {
        return stringToBridgeResponse(frontendService.getWidgetHtml(headers.get("uri")));
    }

    public BackendBridgeResponse localTokenProviderLogin(String uri, String loginJson, Map<String, String> headers) {
        try {
            Login login = objectMapper.readValue(loginJson, Login.class);
            String code = frontendService.localTokenProviderLogin(login);
            return new BackendBridgeResponse("{\"code\":\"" + code + "\",\"state\":\"" + login.getState() + "\"}", 200, new ValidatorMessages());
        } catch (InvalidCredentialsException | JsonProcessingException e) {
            return new BackendBridgeResponse(e.getMessage(), 401, new ValidatorMessages());
        }
    }

    public BackendBridgeResponse localTokenProviderGetTokens(String uri, Map<String, String> headers) {
        String code = headers.get("code");
        String state = headers.get("state");
        try {
            BearerTokens tokens = frontendService.localTokenProviderGetTokens(code, state);
            return toBridgeResponse(tokens);
        } catch (AuthenticationException e) {
            return new BackendBridgeResponse(e.getMessage(), 401, new ValidatorMessages());
        }
    }

    public BackendBridgeResponse renewApiTokens(String uri, Map<String, String> headers) {
        try {
            return toBridgeResponse(frontendService.processRenewApiTokenRequest(headers.get("renewToken")));
        } catch (Exception e) {
            return new BackendBridgeResponse(e.getMessage(), 500, new ValidatorMessages());
        }
    }

    private ClientRequest request(String requestJson, Map<String, String> headers) {
        try {
            var request = objectMapper.readValue(requestJson, ClientRequest.class);
            request.setLocale(Locale.GERMANY); // TODO konfigurierbar machen
            request.setZoneId("Europe/Berlin");
            var authenticationHeader = headers.get("Authorization");
            if (authenticationHeader != null && authenticationHeader.startsWith("Bearer ")) {
                request.setAccessToken(authenticationHeader.substring("Bearer ".length()));
            }
            return request;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize request", e);
        }
    }

    private <T> BackendBridgeResponse toBridgeResponse(T o) {
        return new BackendBridgeResponse(serialialize(o), 200, new ValidatorMessages());
    }

    private <T> BackendBridgeResponse toBridgeResponse(ServerResponse o) {
        var response = new BackendBridgeResponse(serialialize(o), o.getStatus(), o.getValidatorMessages());
        if (o.getTokens() != null) {
            response.addResponseHeader("Set-Cookie", "access_token=" + o.getTokens().getAccessToken() + "; HttpOnly; Secure; Path=/; SameSite=Strict");
            response.addResponseHeader("Set-Cookie", "refresh_token=" + o.getTokens().getRenewToken() + "; HttpOnly; Secure; Path=/; SameSite=Strict");
        }
        return response;
    }

    private String serialialize(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize", e);
        }
    }

    private BackendBridgeResponse stringToBridgeResponse(String str) {
        return new BackendBridgeResponse(str, 200, new ValidatorMessages());
    }

    @Override
    public String getCustomStaticResourcePath() {
        return "public";
    }
}
