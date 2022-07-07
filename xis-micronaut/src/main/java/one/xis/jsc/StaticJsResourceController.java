package one.xis.jsc;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.xis.resource.ReloadableResourceFile;
import one.xis.resource.ResourceFile;
import one.xis.resource.ResourceFiles;

@Controller(produces = "text/javascript; charset=utf-8")
class StaticJsResourceController {

    @Inject
    private MicronautContextAdapter contextAdapter;
    private ResourceFiles resourceFiles;

    @PostConstruct
    void init() {
        resourceFiles = contextAdapter.getResourceFiles();
    }

    @Get("/xis/api/functions.js")
    String getFunctions() {
        return resourceFiles.getByPath("js/functions.js").getContent();
    }

    @Get("/xis/api/base-classes.js")
    String getBaseClasses() {
        return resourceFiles.getByPath("js/base-classes.js").getContent();
    }

    @Get("/xis/api/classes.js")
    String getApi() {
        return resourceFiles.getByPath("js/classes.js").getContent();
    }

    @Get("/xis/api/xis-initializer.js")
    String getPageInitializer() {
        return resourceFiles.getByPath("js/xis-initializer.js").getContent();
    }

    @Get("/xis/api/xis-globals.js")
    String getGlobals() {
        return resourceFiles.getByPath("js/xis-globals.js").getContent();
    }

    @Get("/xis/api/custom-script.js")
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
