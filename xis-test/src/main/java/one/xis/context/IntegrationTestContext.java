package one.xis.context;


import one.xis.server.FrontendService;
import one.xis.server.PageUtil;
import one.xis.server.Request;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IntegrationTestContext {

    private final AppContext internalContext;
    private final FrontendService frontendService;
    private final Map<String, Object> pageControllers;

    public IntegrationTestContext(Object... controllers) {
        internalContext = AppContextBuilder.createInstance()
                .withPackage("one.xis")
                .withSingeltons(controllers)
                .build();
        frontendService = internalContext.getSingleton(FrontendService.class);
        pageControllers = Arrays.stream(controllers).collect(Collectors.toMap(PageUtil::getUrl, Function.identity()));
    }

    public void openPage(String uri, Map<String, Object> parameters, String clientId, String userId) {
        Object controller = pageControllers.get(uri); // TODO Exception if null
        Request request = new Request();
        request.setClientId(clientId);
        request.setUserId(userId);
        request.setControllerId(uri);
        // TODO Vieleicht zu kompliziert.

    }
}
