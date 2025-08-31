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
     * Returns the HTML content of the root page, which is the skeleton of the frontend application. It will
     * not get replaced by any other page, but the page's content and head will get merged into it.
     *
     * @return The HTML content of the root page.
     */
    String getRootPageHtml();

    /**
     * Returns the content of the app.js file, which is the main JavaScript file for the frontend application.
     * This file is responsible for initializing the application and setting up the main components.
     * This is not used for production builds, but it is useful for development and debugging purposes.
     *
     * @return The URL of the app.js file.
     */
    String getAppJs();

    /**
     * Returns the content of the classes.js file, which contains the JavaScript classes
     * used by the frontend application. This is not used for production builds,
     * but it is useful for development and debugging purposes.
     *
     * @return The URL of the classes.js file.
     */
    String getClassesJs();

    /**
     * Returns the content of the main.js file, which is the entry point for the frontend application.
     * This file is responsible for initializing the application and setting up the main components.
     * This is not used for production builds, but it is useful for development and debugging purposes.
     *
     * @return The URL of the main.js file.
     */
    String getMainJs();

    /**
     * Returns the content of the functions.js file, which contains utility functions
     * used by the frontend application. This is not used for production builds,
     * but it is useful for development and debugging purposes.
     *
     * @return The URL of the functions.js file.
     */
    String getFunctionsJs();

    /**
     * Returns the URL of the bundle.js file, which contains the frontend code.
     * This file is loaded by the browser to run the frontend application.
     *
     * @return The URL of the bundle.js file.
     */
    String getBundleJs();


    String getBundleJsMap();

}
