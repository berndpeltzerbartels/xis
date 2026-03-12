package one.xis.ws.spring;

import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.gson.GsonProvider;
import one.xis.server.ClientConfigService;
import one.xis.ws.WSService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
class SpringWebSocketInitializer {

    private final Collection<SpringWSHandlerSPI> wsHandlers;

    private final GsonProvider gsonProvider;
    private final ClientConfigService clientConfigService;

    @Init
    public void init() {
        WSService service = new WSService(gsonProvider, clientConfigService);
        for (SpringWSHandlerSPI handler : wsHandlers) {
            handler.setWSService(service);
        }
    }
}
