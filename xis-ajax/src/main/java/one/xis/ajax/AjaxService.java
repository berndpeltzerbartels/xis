package one.xis.ajax;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.page.PageService;
import one.xis.widget.WidgetService;

@XISComponent
@RequiredArgsConstructor
public class AjaxService {

    public static final String CLIENT_ID_HEADER_NAME = "X-XIS-CLIENT-ID";

    private final PageService pageService;
    private final WidgetService widgetService;
    private final AjaxRequestContextFactory requestContextFactory;

    public AjaxResponse handleRequest(AjaxRequest request, String clientId, String authorization) {
        var response = new AjaxResponse();
        var requestContext = requestContextFactory.createContext(request, clientId, authorization);
        requestContext.getInvocationContexts().forEach(invocationContext -> {
            switch (request.getComponentType()) {
                case WIDGET:
                    response.addResponseMessages(widgetService.invokeController(invocationContext));
                    break;
                case PAGE:
                    response.addResponseMessages(pageService.invokeController(invocationContext));
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        });
        return response;
    }
}