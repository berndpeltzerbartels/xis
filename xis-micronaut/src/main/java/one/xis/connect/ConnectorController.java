package one.xis.connect;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.xis.ConnectorRequest;
import one.xis.ConnectorResponse;
import one.xis.ConnectorService;
import one.xis.micronaut.MicronautContextAdapter;

@Controller
class ConnectorController {

    @Inject
    private MicronautContextAdapter adapter;
    private ConnectorService connectorService;

    @PostConstruct
    void init() {
        connectorService = adapter.getConnectorService();
    }

    @Post(consumes = "application/json", produces = "application/json", uri = "/xis/connector")
    ConnectorResponse handleMessage(ConnectorRequest request) {
        return connectorService.handleMessage(request);
    }

}
