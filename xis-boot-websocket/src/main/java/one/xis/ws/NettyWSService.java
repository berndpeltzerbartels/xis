package one.xis.ws;

import one.xis.context.Component;
import one.xis.gson.GsonProvider;
import one.xis.server.FrontendService;

import java.util.Collection;


@Component
class NettyWSService extends WSService {

    NettyWSService(FrontendService frontendService, GsonProvider gsonProvider, Collection<WSExceptionHandler<?>> exceptionHandlers) {
        super(frontendService, gsonProvider, exceptionHandlers);
    }
}
