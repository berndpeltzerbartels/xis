package one.xis.js;

import one.xis.resource.Resource;

public interface JavascriptProvider {
    Resource getCompressedJavascript();

    Resource getSourceMap();
}
