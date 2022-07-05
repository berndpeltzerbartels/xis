package one.xis.jsc;

import one.xis.resource.ResourceFile;
import one.xis.resource.ResourceFiles;
import one.xis.spring.servlet.ResourceServlet;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet("/xis/xis-initializer.js")
class InitializerServlet extends ResourceServlet {

    @Autowired
    private ResourceFiles resourceFiles;

    @Override
    protected ResourceFile getResource(HttpServletRequest request) {
        return resourceFiles.getByPath("xis-initializer.js");
    }

    @Override
    protected String getContentType() {
        return "text/javascript; charset=utf-8";
    }
}
