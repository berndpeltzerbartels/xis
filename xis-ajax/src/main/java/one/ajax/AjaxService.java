package one.ajax;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.dto.ActionRequest;
import one.xis.dto.ActionResponse;
import one.xis.dto.InitialResponse;
import one.xis.dto.ModelRequest;
import one.xis.page.PageService;
import one.xis.widget.WidgetService;

@XISComponent
@RequiredArgsConstructor
public class AjaxService {

    private final PageService pageService;
    private final WidgetService widgetService;

    public InitialResponse handlePageInitialRequest(ModelRequest modelRequest) {
        return pageService.invokeInitial(modelRequest);
    }
    
    public ActionResponse handlePageActionRequest(ActionRequest request) {
        return pageService.invokeAction(request);
    }

    public InitialResponse handleWidgetInitialRequest(ModelRequest modelRequest) {
        return widgetService.invokeInitial(modelRequest);
    }


    public ActionResponse handleWidgetActionRequest(ActionRequest request) {
        return widgetService.invokeAction(request);
    }

}