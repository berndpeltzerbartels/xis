package one.ajax;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.dto.ActionRequest;
import one.xis.dto.ModelRequest;
import one.xis.dto.ModelResponse;
import one.xis.dto.RequestIssue;
import one.xis.page.PageService;
import one.xis.widget.WidgetService;

@XISComponent
@RequiredArgsConstructor
public class AjaxService {

    private final PageService pageService;
    private final WidgetService widgetService;

    public ModelResponse handlePageModelRequest(ModelRequest modelRequest) {
        var response = new ModelResponse();
        response.setComponentModel();
        var result = pageService.invokeGetModel(modelRequest);
    }


    public ModelResponse handlePageActionRequest(ActionRequest request) {
        return null;
    }

    public ModelResponse handleWidgetModelRequest(ModelRequest modelRequest) {
        return null;
    }


    public ModelResponse handleWidgetActionRequest(ActionRequest request) {
        return null;
    }

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