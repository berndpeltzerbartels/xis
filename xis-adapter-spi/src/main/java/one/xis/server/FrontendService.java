package one.xis.server;

import one.xis.resource.Resource;

/**
 * The FrontendService interface defines the contract for the frontend service in the XIS (Xis Integration Service) system.
 * It represents the next layer below the controller layer and is responsible for handling requests related to the frontend application.
 * It is used for every framework.
 */
public interface FrontendService {

    /**
     * Returns the configuration of the frontend service. It contains the list of pages, widgets, and other configuration details.
     * Its content is not secret, and contains information required to run the frontend application.
     * .
     *
     * @return The ClientConfig object containing the configuration details.
     */
    ClientConfig getConfig();

    /**
     * Processes an action (page or widget) request. This response typically contains
     * and identifier for the next page to be displayed, or the widget to be updated, but it may also contain
     * other information, such as a redirect URL or a message to be displayed etc.
     *
     * @param request The client request containing the necessary information.
     * @return A ServerResponse containing the HTML content of the page.
     */
    ServerResponse processActionRequest(ClientRequest request);

    /**
     * Processes a request to retrieve the model data for a page or widget.
     *
     * @param request The client request containing the necessary information.
     * @return A ServerResponse containing the model data for the page.
     */
    ServerResponse processModelDataRequest(ClientRequest request);

    /**
     * Processes a request to retrieve the model data for a form (on page or widget).
     *
     * @param request The client request containing the necessary information.
     * @return A ServerResponse containing the model data for the form.
     */
    ServerResponse processFormDataRequest(ClientRequest request);

    /**
     * String getPageHead(String id);
     * <p>
     * /**
     * Returns the HTML template of the body element for a page with the given ID.
     *
     * @param id The ID of the page.
     * @return The HTML content of the body element.
     */
    Resource getPageHead(String id);

    /**
     * Returns the HTML content of the body element for a page with the given ID.
     *
     * @param id The ID of the page.
     * @return The HTML content of the body element.
     */
    Resource getPageBody(String id);

    /**
     * Returns the attributes of the body element for a page with the given ID.
     *
     * @param id The ID of the page.
     * @return BodyAttributesResource containing the attributes and lastModified.
     */
    Resource getBodyAttributes(String id);

    /**
     * Returns the HTML template of a widget with the given ID.
     *
     * @param id The ID of the widget.
     * @return The HTML content of the widget.
     */
    Resource getWidgetHtml(String id);

    /**
     * Returns the HTML content of an include with the given key.
     *
     * @param key The key of the include.
     * @return The HTML content of the include.
     */
    Resource getIncludeHtml(String key);

    /**
     * Returns the HTML content of the root page, which is the skeleton of the frontend application. It will
     * not get replaced by any other page, but the page's content and head will get merged into it.
     *
     * @return The HTML content of the root page.
     */
    String getRootPageHtml();

    /**
     * Returns the URL of the bundle.js file, which contains the frontend code.
     * This file is loaded by the browser to run the frontend application.
     *
     * @return The URL of the bundle.js file.
     */
    Resource getBundleJs();


    Resource getBundleJsMap();

}
