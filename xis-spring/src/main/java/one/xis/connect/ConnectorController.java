package one.xis.connect;

import one.xis.ConnectorRequest;
import one.xis.ConnectorResponse;
import one.xis.ConnectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
class ConnectorController {

    @Autowired
    private ConnectorService connectorService;

    @PostMapping("/xis/connector")
    ConnectorResponse handleMessage(ConnectorRequest request) {
        return connectorService.handleMessage(request);
    }
}
