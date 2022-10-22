package one.xis.page;

import lombok.Builder;
import lombok.Getter;
import one.xis.resource.ResourceFile;

@Builder
@Getter
class PageMetaData {
    private final String id;
    private final String path;
    private final boolean welcomePage;
    private final ResourceFile htmlTemplate;
    private final String javascriptClassname;
    private final String controllerClass;

}
