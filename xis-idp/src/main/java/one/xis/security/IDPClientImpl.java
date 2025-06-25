package one.xis.security;


import one.xis.context.XISComponent;
import one.xis.server.ApiTokens;

@XISComponent
class IDPClientImpl implements IDPClient {
    @Override
    public ApiTokens requestTokens(String code, String state) throws AuthenticationException {
        return null;
    }
}
