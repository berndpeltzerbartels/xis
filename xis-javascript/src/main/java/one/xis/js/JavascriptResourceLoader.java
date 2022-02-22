package one.xis.js;

import one.xis.utils.io.IOUtils;

class JavascriptResourceLoader {

    private static String javascript;
    private static long lastModified;
    private static JavascriptResource XISResource;

    static {
        javascript = getJavascript();
        lastModified = getLastModified();
        XISResource = new JavascriptResource(lastModified, javascript);
    }

    public static synchronized JavascriptResource getResource() {
        if (getLastModified() > lastModified) {
            javascript = getJavascript();
            lastModified = getLastModified();
            XISResource = new JavascriptResource(lastModified, javascript);
        }
        return XISResource;
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
