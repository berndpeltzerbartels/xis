package one.xis;

import lombok.RequiredArgsConstructor;
import one.xis.common.RequestIssue;
import one.xis.context.XISComponent;
import one.xis.page.PageService;
import one.xis.widget.WidgetService;

@XISComponent
@RequiredArgsConstructor
public class ConnectorService {

    private final PageService pageService;
    private final WidgetService widgetService;

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
        switch (request.getIssue()) {
            case MODEL:
                return new ConnectorResponse(pageService.invokeGetModel(request), RequestIssue.MODEL);
            case ACTION:
                return new ConnectorResponse(pageService.invokeAction(request), RequestIssue.ACTION);
            default:
                throw new IllegalStateException();
        }
    }

    private ConnectorResponse handleWidgetMessage(ConnectorRequest request) {
        switch (request.getIssue()) {
            case MODEL:
                return new ConnectorResponse(widgetService.invokeGetModel(request), RequestIssue.MODEL);
            case ACTION:
                return new ConnectorResponse(widgetService.invokeAction(request), RequestIssue.ACTION);
            default:
                throw new IllegalStateException();
        }
    }
}