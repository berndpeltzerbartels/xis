package one.xis;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.page.PageService;

@XISComponent
@RequiredArgsConstructor
public class ConnectorService {

    private final PageService pageService;

    public ConnectorResponse handleMessage(ConnectorRequest request) {
        switch (request.getComponentType()) {
            case PAGE:
                return handlePageMessage(request);
            case WIDGET:
                return handleWidgetMessage(request);
            default:
                throw new IllegalArgumentException("component-type: " + request.getComponentType());
        }
    }

    private ConnectorResponse handlePageMessage(ConnectorRequest request) {
        var model = pageService.invokeInit(request);
        return new ConnectorResponse(model);
    }

    private ConnectorResponse handleWidgetMessage(ConnectorRequest request) {
        return null;
    }
}