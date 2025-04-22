package one.xis.boot.resources;

import one.xis.context.XISComponent;
import one.xis.server.ResourcePathProvider;

@XISComponent
class XISBootResourcePathProvider implements ResourcePathProvider {
    @Override
    public String getCustomStaticResourcePath() {
        return "public";
    }
}
