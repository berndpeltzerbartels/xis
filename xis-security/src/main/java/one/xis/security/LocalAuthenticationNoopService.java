package one.xis.security;


/**
 * A no-operation implementation of the LocalAuthenticationProviderService. There is a dependency that
 * requires an implementation of LocalAuthenticationProviderService. This class serves as a placeholder
 * for environments where local authentication is not supported, such as in a cloud environment.
 */
class LocalAuthenticationNoopService implements LocalAuthenticationProviderService {

    private static final String ERROR_MESSAGE = "Local authentication is not supported in this environment. Please use an external authentication provider.";

    @Override
    public String login(String user, String password) throws InvalidCredentialsException {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public LocalAuthenticationTokenResponse issueToken(String code, String state) throws AuthenticationException {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public LocalAuthenticationTokenResponse refresh(String refreshToken) throws InvalidTokenException, AuthenticationException {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public LocalUserInfo getUserInfo(String accessToken) throws InvalidTokenException {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

}
