package one.xis.ws;

import one.xis.context.Component;
import one.xis.gson.GsonProvider;
import one.xis.server.FrontendService;


@Component
class NettyWSService extends WSService {

    NettyWSService(FrontendService frontendService, GsonProvider gsonProvider) {
        super(frontendService, gsonProvider);
    }
}
