package one.xis.widget;

import lombok.Builder;
import lombok.Getter;
import one.xis.resource.ResourceFile;

@Builder
@Getter
class WidgetMetaData {
    private final String id;
    private final ResourceFile htmlTemplate;
    private final String javascriptClassname;
    private final Class<?> controllerClass;

    public String getControllerClassName() {
        return controllerClass.getName();
    }
}
