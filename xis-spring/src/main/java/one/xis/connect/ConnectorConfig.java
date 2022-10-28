package one.xis.connect;

import one.xis.ConnectorService;
import one.xis.context.AppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
class ConnectorConfig {

    @Autowired
    private AppContext appContext;
    private ConnectorService connectorService;

    @PostConstruct
    void init() {
        connectorService = appContext.getSingleton(ConnectorService.class);
    }
    
    @Bean
    ConnectorService connectorService() {
        return connectorService;
    }
}
