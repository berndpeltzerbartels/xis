package one.xis.js;

import one.xis.resource.ResourceFile;

public interface JavascriptComponent {

    boolean isCompiled();

    void setJavascript(String javascript);

    ResourceFile getHtmlResourceFile();

    void setCompiled(boolean compiled);

    String getJavascriptClass();

    Class<?> getControllerClass();

    default String getControllerClassName() {
        return getControllerClass().getName();
    }
}
