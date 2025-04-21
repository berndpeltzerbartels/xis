package one.xis.boot.resources;

import one.xis.context.XISComponent;
import one.xis.server.StaticResourcePathProvider;

@XISComponent
class PublicResourcePathProvider implements StaticResourcePathProvider {
    @Override
    public String getCustomStaticResourcePath() {
        return "public";
    }
}
