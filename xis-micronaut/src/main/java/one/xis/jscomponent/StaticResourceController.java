package one.xis.jscomponent;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.xis.resource.ReloadableResourceFile;
import one.xis.resource.ResourceFile;
import one.xis.resource.ResourceFiles;

@Controller(produces = "text/javascript; charset=utf-8")
class StaticResourceController {

    @Inject
    private MicronautContextAdapter contextAdapter;
    private ResourceFiles resourceFiles;

    @PostConstruct
    void init() {
        resourceFiles = contextAdapter.getResourceFiles();
    }

    @Get("/xis/xis-api.js")
    String getApi() {
        return resourceFiles.getByPath("xis-api.js").getContent();
    }

    @Get("/xis/xis-initializer.js")
    String getPageInitializer() {
        return resourceFiles.getByPath("xis-initializer.js").getContent();
    }

    @Get("/xis/custom-script.js")
    String getCustomScript() {
        if (resourceFiles.exists(RootPage.CUSTOM_SCRIPT)) {
            ResourceFile customScript = resourceFiles.getByPath(RootPage.CUSTOM_SCRIPT);
            if (customScript instanceof ReloadableResourceFile) {
                ReloadableResourceFile reloadableResourceFile = (ReloadableResourceFile) customScript;
                if (reloadableResourceFile.isObsolete()) {
                    reloadableResourceFile.reload();
                }
            }
            return customScript.getContent();
        }
        return "";
    }
}
