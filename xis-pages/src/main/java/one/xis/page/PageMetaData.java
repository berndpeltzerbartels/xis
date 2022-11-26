package one.xis.page;

import lombok.Builder;
import lombok.Getter;
import one.xis.resource.ResourceFile;

@Builder
@Getter
public class PageMetaData {
    private final String path;
    private final boolean welcomePage;
    private final ResourceFile htmlTemplate;
    private final String javascriptClassname;
    private final Class<?> controllerClass;
}
