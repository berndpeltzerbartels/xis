package one.xis.idp;

import lombok.RequiredArgsConstructor;
import one.xis.auth.token.ApiTokensAndUrl;
import one.xis.auth.token.StateParameter;
import one.xis.context.XISComponent;
import one.xis.ipdclient.IDPClientService;

import java.util.Map;

import static one.xis.utils.http.HttpUtils.parseQueryParameters;


@XISComponent
@RequiredArgsConstructor
class IDPFrontendServiceImpl implements IDPFrontendService {

    private final IDPClientService idpClientService;

    @Override
    public ApiTokensAndUrl authenticationCallback(String provider, String queryString) {
        Map<String, String> queryParams = parseQueryParameters(queryString);
        String state = queryParams.get("state");
        String code = queryParams.get("code");
        if (state == null || state.isEmpty()) {
            throw new IllegalArgumentException("Missing or empty 'state' parameter in the query string");
        }
        var stateParameterPayload = StateParameter.decodeAndVerify(state);
        // Local authentication is handled by a controller, instead of the IDP service.
        var tokens = idpClientService.fetchNewTokens(provider, code, state);
        var authenticationData = new ApiTokensAndUrl();
        authenticationData.setApiTokens(tokens);
        authenticationData.setUrl(stateParameterPayload.getRedirect());
        return authenticationData;
    }

    @Override
    public String createLoginFormUrl(String provider, String redirectUri) {
        return idpClientService.getIDPLoginFormUrl(provider, redirectUri);
    }

}
