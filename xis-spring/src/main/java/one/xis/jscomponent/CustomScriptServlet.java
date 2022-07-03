package one.xis.jscomponent;

import one.xis.resource.ReloadableResourceFile;
import one.xis.resource.ResourceFile;
import one.xis.resource.ResourceFiles;
import one.xis.spring.servlet.ResourceServlet;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet("/xis/custom-script.js")
class CustomScriptServlet extends ResourceServlet {

    @Autowired
    private ResourceFiles resourceFiles;

    @Override
    protected ResourceFile getResource(HttpServletRequest request) {
        if (resourceFiles.exists(RootPage.CUSTOM_SCRIPT)) {
            ResourceFile customScript = resourceFiles.getByPath(RootPage.CUSTOM_SCRIPT);
            if (customScript instanceof ReloadableResourceFile) {
                ReloadableResourceFile reloadableResourceFile = (ReloadableResourceFile) customScript;
                if (reloadableResourceFile.isObsolete()) {
                    reloadableResourceFile.reload();
                }
            }
            return customScript;
        }
        return ResourceFile.EMPTY_FILE;
    }

    @Override
    protected String getContentType() {
        return "text/javascript; charset=utf-8";
    }
}
