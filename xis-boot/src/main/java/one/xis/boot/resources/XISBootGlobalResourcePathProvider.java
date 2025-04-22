package one.xis.boot.resources;

import one.xis.context.XISComponent;
import one.xis.server.GlobalResourcePathProvider;

@XISComponent
class XISBootGlobalResourcePathProvider implements GlobalResourcePathProvider {
    @Override
    public String getCustomStaticResourcePath() {
        return "public/global";
    }
}
