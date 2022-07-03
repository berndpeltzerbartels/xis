package one.xis.jscomponent;

import one.xis.resource.ResourceFile;

interface JavascriptComponent {

    boolean isCompiled();

    void setJavascript(String javascript);

    ResourceFile getHtmlResourceFile();

    void setCompiled(boolean compiled);
}
