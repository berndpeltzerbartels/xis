package one.xis.js;

import one.xis.utils.io.IOUtils;

class JSResourceLoader {

    private static String javascript;
    private static long lastModified;
    private static JSResource jsResource;

    static {
        javascript = getJavascript();
        lastModified = getLastModified();
        jsResource = new JSResource(lastModified, javascript);
    }

    public static synchronized JSResource getJsResource() {
        if (getLastModified() > lastModified) {
            javascript = getJavascript();
            lastModified = getLastModified();
            jsResource = new JSResource(lastModified, javascript);
        }
        return jsResource;
    }


    private static String getJavascript() {
        return IOUtils.getResourceAsString("xis-template-functions.js") +
                IOUtils.getResourceAsString("xis-template-classes.js") +
                IOUtils.getResourceAsString("xis-generated.js");
    }

    private static long getLastModified() {
        return IOUtils.getResourceLastModified("xis-generated.js");
    }
}
