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

    @Get("/xis/api/classes1.js")
    String getClasses1() {
        return resourceFiles.getByPath("js/classes1.js").getContent();
    }

    @Get("/xis/api/classes2.js")
    String getClasses2() {
        return resourceFiles.getByPath("js/classes2.js").getContent();
    }

    @Get("/xis/api/classes3.js")
    String getClasses3() {
        return resourceFiles.getByPath("js/classes3.js").getContent();
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
