package one.xis.js;

import one.xis.resource.ResourceFile;

/**
 * Representation of the client-side part of acomponent,
 * mainly holder of the corresponding javascipt.
 */
public interface JavascriptComponent {

    /**
     * @return
     */
    boolean isCompiled();

    /**
     * @param javascript
     */
    void setJavascript(String javascript);

    /**
     * @return Location for the template of this
     * component
     */
    ResourceFile getHtmlResourceFile();

    /**
     * @param compiled
     */
    void setCompiled(boolean compiled);

    /**
     * Name of the components javascript-class
     *
     * @return
     */
    String getJavascriptClass();

    /**
     * Provides the class of the customer-created controller for this
     * client-side component.
     *
     * @return The controller-class
     */
    Class<?> getControllerClass();

    /**
     * @return
     */
    default String getControllerClassName() {
        return getControllerClass().getName();
    }
}
