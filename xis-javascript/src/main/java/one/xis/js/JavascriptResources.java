package one.xis.js;

import one.xis.utils.io.IOUtils;

class JavascriptResources {

    private static JavascriptGenerator generator = new JavascriptGenerator();
    private static long lastModified = System.currentTimeMillis();

    static String getJavascript() {
        return IOUtils.getResourceAsString("xis-template-functions.js") +
                IOUtils.getResourceAsString("xis-template-classes.js") +
                generator.generateJavascript();
    }

    static JavascriptResource getResource() {
        return new JavascriptResource(lastModified, getJavascript());
    }
}
