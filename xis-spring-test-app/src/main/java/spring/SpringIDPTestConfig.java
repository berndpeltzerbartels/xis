package spring;

import one.xis.auth.idp.ExternalIDPConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
class SpringIDPTestConfig implements ExternalIDPConfig {

    @Override
    public String getIdpServerUrl() {
        return "http://localhost:8081";
    }

    @Override
    public String getClientId() {
        return "client-app";
    }

    @Override
    public String getClientSecret() {
        return "client-secret";
    }

    @Override
    public String getIdpId() {
        return "test-idp";
    }

}
