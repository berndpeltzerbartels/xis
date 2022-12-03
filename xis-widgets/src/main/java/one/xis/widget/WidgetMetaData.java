package one.xis.widget;

import lombok.Builder;
import lombok.Getter;
import one.xis.resource.Resource;

@Builder
@Getter
public class WidgetMetaData {
    private final Resource htmlTemplate;
    private final String javascriptClassname;
    private final Class<?> controllerClass;
    private final String key;
}
