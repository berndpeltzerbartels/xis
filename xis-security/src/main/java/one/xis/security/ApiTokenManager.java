package one.xis.security;

public interface ApiTokenManager {

    TokenResult createTokens(TokenRequest tokenRequest) throws InvalidTokenException;

    TokenAttributes decodeToken(String token) throws InvalidTokenException;

    TokenResult renew(String token) throws InvalidTokenException;
}
