package one.xis.jsc;

import one.xis.resource.ReloadableResourceFile;
import one.xis.resource.ResourceFile;
import one.xis.resource.ResourceFiles;
import one.xis.spring.servlet.ResourceServlet;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet("/xis/api/*")
class ApiServlet extends ResourceServlet {

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
