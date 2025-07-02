package one.xis.server;

import java.util.Locale;
import java.util.Map;

public interface FrameworkController<RESP_WRAPPER, REQ, RESP> {

    ClientConfig getComponentConfig(REQ request);

    RESP_WRAPPER getPageModel(ClientRequest request, String authenticationHeader, Locale locale);

    RESP_WRAPPER getFormModel(ClientRequest request, String authenticationHeader, Locale locale);

    RESP_WRAPPER getWidgetModel(ClientRequest request, String authenticationHeader, Locale locale);

    RESP_WRAPPER onPageLinkAction(ClientRequest request, String authenticationHeader, Locale locale);

    RESP_WRAPPER onWidgetLinkAction(ClientRequest request, String authenticationHeader, Locale locale);

    RESP onFormAction(ClientRequest request, String authenticationHeader, Locale locale);

    RESP idpGetTokens(String code, String state);

    /**
     * Authenticates a user with the given request and provider. This is the callback url
     * the authentication provider will redirect to after successful authentication.
     *
     * @param request  the authentication request containing user credentials
     * @param provider the authentication provider to use
     * @return a response containing the authentication result
     */
    RESP authenticationCallback(REQ request, String provider);

    RESP renewApiTokens(String renewToken);

    String getPageHead(String id);

    String getPageBody(String id);

    Map<String, String> getBodyAttributes(String id);

    String getWidgetHtml(String id);

    String getAppJs();

    String getClassesJs();

    String getMainJs();

    String getFunctionsJs();

    String getBundleJs();


    default String token(String authenticationHeader) {
        if (authenticationHeader != null && authenticationHeader.startsWith("Bearer ")) {
            return authenticationHeader.substring("Bearer ".length());
        }
        return null;
    }
}
