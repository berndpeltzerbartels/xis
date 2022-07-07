package one.xis.jsc;

import one.xis.resource.ReloadableResourceFile;
import one.xis.resource.ResourceFile;
import one.xis.resource.ResourceFiles;
import one.xis.spring.servlet.ResourceServlet;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

/*
    @Get("/xis/functions.js")
    String getFunctions() {
        return resourceFiles.getByPath("js/functions.js").getContent();
    }

    @Get("/xis/base-classes.js")
    String getBaseClasses() {
        return resourceFiles.getByPath("js/base-classes.js").getContent();
    }

    @Get("/xis/classes.js")
    String getApi() {
        return resourceFiles.getByPath("js/classes.js").getContent();
    }

    @Get("/xis/xis-initializer.js")
    String getPageInitializer() {
        return resourceFiles.getByPath("xis-initializer.js").getContent();
    }

    @Get("/xis/xis-globals.js")
    String getGlobals() {
        return resourceFiles.getByPath("xis-globals.js").getContent();
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
 */
@WebServlet("/xis/api/*")
class StaticJsResourceServlet extends ResourceServlet {

    @Autowired
    private ResourceFiles resourceFiles;

    @Override
    protected ResourceFile getResource(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String file = uri.substring(uri.lastIndexOf('/') + 1);
        if (file.equals(RootPage.CUSTOM_SCRIPT)) {
            return getCustomerScipt();
        }
        return resourceFiles.getByPath("js/" + file);
    }

    @Override
    protected String getContentType() {
        return "text/javascript; charset=utf-8";
    }


    private ResourceFile getCustomerScipt() {
        if (resourceFiles.exists(RootPage.CUSTOM_SCRIPT)) {
            ResourceFile customScript = resourceFiles.getByPath(RootPage.CUSTOM_SCRIPT);
            if (customScript instanceof ReloadableResourceFile) {
                ReloadableResourceFile reloadableResourceFile = (ReloadableResourceFile) customScript;
                if (reloadableResourceFile.isObsolete()) {
                    reloadableResourceFile.reload();
                }
            }
            return customScript;
        } else {
            return ResourceFile.EMPTY_FILE;
        }
    }
}
