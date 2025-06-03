package one.xis.security;

public interface LocalAuthentication {

    String login(String user, String password) throws InvalidCredentialsException;

    LocalAuthenticationTokenResponse issueToken(String code, String state) throws AuthenticationException;

    LocalAuthenticationTokenResponse refresh(String refreshToken) throws InvalidTokenException, AuthenticationException;

    LocalUserInfo getUserInfo(String accessToken) throws InvalidTokenException;

}
