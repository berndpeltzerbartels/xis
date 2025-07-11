package one.xis;


import one.xis.context.XISDefaultComponent;

@XISDefaultComponent
class DefaultLoginUrlProvider implements LoginUrlProvider {
    @Override
    public String loginUrl(String redirectUri) {
        throw new UnsupportedOperationException("Login URL provider is not configured. " +
                "No authentication-dependency module is present in the classpath and no custom LoginUrlProvider is registered. ");
    }
}
