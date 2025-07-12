package one.xis.auth;


import one.xis.context.XISDefaultComponent;

@XISDefaultComponent
class DefaultLoginUrlProvider implements LoginUrlProvider {
    @Override
    public String loginUrl(String redirectUri) {
        throw new UnsupportedOperationException("Login URL provider is not configured. " +
                "Check if an authentication module is included in the application ");
    }
}
