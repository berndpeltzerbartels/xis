package one.xis.server;

import one.xis.resource.Resource;

import java.util.Locale;

public interface FrameworkController<RESP_WRAPPER, REQ, RESP> {

    ClientConfig getComponentConfig(REQ request);

    RESP_WRAPPER getPageModel(ClientRequest request, String authenticationHeader, Locale locale);

    RESP_WRAPPER getFormModel(ClientRequest request, String authenticationHeader, Locale locale);

    RESP_WRAPPER getWidgetModel(ClientRequest request, String authenticationHeader, Locale locale);

    RESP_WRAPPER onPageLinkAction(ClientRequest request, String authenticationHeader, Locale locale);

    RESP_WRAPPER onWidgetLinkAction(ClientRequest request, String authenticationHeader, Locale locale);

    RESP onFormAction(ClientRequest request, String authenticationHeader, Locale locale);

    RESP getIdpTokens(String payload);

    Resource getPageHead(String id);

    Resource getPageBody(String id);

    Resource getBodyAttributes(String id);

    Resource getWidgetHtml(String id);

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

    RESP getOpenIdConfiguration();

    RESP getIdpPublicKey();

    RESP authenticationCallback(String code, String state, String idpId);
}
