package one.xis.server;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;

@XISComponent
@RequiredArgsConstructor
public class FrontendService {

    private final ControllerService controllerService;
    private final ConfigService configService;

    @Getter
    private Config config;

    @XISInit
    void init() {
        config = configService.getConfig();
    }

    public Response invokePageActionMethod(Request request) {
        return controllerService.invokePageActionMethod(request);
    }

    public Response invokeWidgetActionMethod(Request request) {
        return controllerService.invokeWidgetActionMethod(request);
    }

    public Response invokePageModelMethods(Request request) {
        return controllerService.invokePageModelMethods(request);
    }

    public Response invokeWidgetModelMethods(Request request) {
        return controllerService.invokeWidgetModelMethods(request);
    }

    public String getPageHtmlResource(String id) {
        return controllerService.getPageHtmlResource(id).getContent();
    }

    public String getWidgetHtmlResource(String id) {
        return controllerService.getWidgetHtmlResource(id).getContent();
    }


}
