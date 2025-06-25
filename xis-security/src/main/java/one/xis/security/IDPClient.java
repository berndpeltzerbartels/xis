package one.xis.security;

import one.xis.server.ApiTokens;

public interface IDPClient {


    ApiTokens requestTokens(String code, String state) throws AuthenticationException;


}
