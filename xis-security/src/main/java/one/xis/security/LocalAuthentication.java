package one.xis.security;

public interface LocalAuthentication {

    LocalAuthenticationCodeResponse login(String user, String password) throws InvalidCredentialsException;

    LocalAuthenticationTokenResponse issueToken(String code, String state) throws InvalidStateParameterException;

    LocalUserInfo getUserInfo(String accessToken) throws InvalidTokenException;

}
