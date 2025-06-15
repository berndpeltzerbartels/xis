package one.xis.server;

import one.xis.security.AuthenticationException;
import one.xis.security.InvalidCredentialsException;
import one.xis.security.Login;

import java.util.Map;

public interface FrontendService {
    ClientConfig getConfig();

    ServerResponse processActionRequest(ClientRequest request);

    ServerResponse processModelDataRequest(ClientRequest request);

    ServerResponse processFormDataRequest(ClientRequest request);

    ServerResponse processLoginRequest(ClientRequest request) throws InvalidCredentialsException;

    ApiTokens processRenewApiTokenRequest(String renewToken);

    AuthenticationData authenticationCallback(String provider, String queryString);

    String localTokenProviderLogin(Login login) throws InvalidCredentialsException;

    BearerTokens localTokenProviderGetTokens(String code, String state) throws AuthenticationException;

    String getPage(String id);

    String getPageHead(String id);

    String getPageBody(String id);

    Map<String, String> getBodyAttributes(String id);

    String getWidgetHtml(String id);

    String getRootPageHtml();

    String getAppJs();

    String getClassesJs();

    String getMainJs();

    String getFunctionsJs();

    String getBundleJs();

    default void extractAccessToken(ClientRequest request, String authenticationHeader) {
        if (authenticationHeader != null && authenticationHeader.startsWith("Bearer ")) {
            request.setAccessToken(authenticationHeader.substring("Bearer ".length()));
        }
    }

}
