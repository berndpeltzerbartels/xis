package one.xis.server;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;

@XISComponent
@RequiredArgsConstructor
public class FrontendService {

    private final ControllerInvocationService invocationService;
    private final ConfigService configService;

    @Getter
    private Config config;

    @XISInit
    void init() {
        config = configService.getConfig();
    }

    public Response invokeModelMethods(Request request) {
        return invocationService.invokeModelMethods(request);
    }

    public Response invokeActionMethod(Request request) {
        return invocationService.invokeActionMethod(request);
    }
}
