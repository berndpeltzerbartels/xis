package one.xis.spring;

import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.ws.WSService;
import one.xis.ws.spring.SpringWSHandlerSPI;

import java.util.Collection;

@Component
@RequiredArgsConstructor
class SpringWebSocketInitializer {

    private final Collection<SpringWSHandlerSPI> wsHandlers;

    private final WSService wsService;

    @Init
    public void init() {
        for (SpringWSHandlerSPI handler : wsHandlers) {
            handler.setWSService(wsService);
        }
    }
}
