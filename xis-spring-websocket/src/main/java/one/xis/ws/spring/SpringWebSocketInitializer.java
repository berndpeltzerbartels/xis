package one.xis.ws.spring;

import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.gson.GsonProvider;
import one.xis.server.FrontendService;
import one.xis.ws.WSExceptionHandler;
import one.xis.ws.WSService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
class SpringWebSocketInitializer {

    private final Collection<SpringWSHandlerSPI> wsHandlers;

    private final FrontendService frontendService;
    private final GsonProvider gsonProvider;
    private final Collection<WSExceptionHandler<?>> exceptionHandlers;

    @Init
    public void init() {
        WSService service = new WSService(frontendService, gsonProvider, exceptionHandlers);
        for (SpringWSHandlerSPI handler : wsHandlers) {
            handler.setWSService(service);
        }
    }
}
