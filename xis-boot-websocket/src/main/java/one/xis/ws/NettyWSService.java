package one.xis.ws;

import one.xis.context.Component;
import one.xis.gson.GsonProvider;
import one.xis.server.ClientConfigService;


@Component
class NettyWSService extends WSService {

    NettyWSService(GsonProvider gsonProvider, ClientConfigService clientConfigService) {
        super(gsonProvider, clientConfigService);
    }
}
