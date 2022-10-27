package one.xis.widget;

import lombok.Builder;
import lombok.Getter;
import one.xis.controller.ControllerModel;
import one.xis.resource.ResourceFile;

@Builder
@Getter
class WidgetMetaData {
    private final String id;
    private final ResourceFile htmlTemplate;
    private final String javascriptClassname;
    private final ControllerModel controllerModel;

    public String getControllerClassName() {
        return controllerModel.getControllerClassName();
    }
}
