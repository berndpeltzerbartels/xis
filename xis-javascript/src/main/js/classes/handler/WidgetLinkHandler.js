/**
 * @class WidgetLinkHandler
 * @extends {TagHandler}
 * @package classes/handler
 * @access public
 * @description This handler is responsible for handling a link to a widget. Backend is not involved.
 * 
 * @property {String} type
 * @property {WidgetContainers} widgetContainers
 * @property {Object} widgetParameters
 * @property {String} targetPageUrl
 * @property {String} targetWidgetUrl
 * @property {String} targetContainerId
 */
class WidgetLinkHandler extends TagHandler {

    /**
     * 
     * @param {Element} element 
     * @param {WidgetContainers} widgetContainers
     */
    constructor(element, widgetContainers) {
        super(element);
        this.type = 'widget-link-handler';
        this.widgetContainers = widgetContainers;
        this.widgetParameters = {};
        this.targetPageUrl = undefined;
        this.targetWidgetUrl = undefined;
        this.targetContainerId = undefined;
        element.setAttribute("href", "#");
        element.addEventListener('click', event => {
            event.preventDefault();
            this.onClick(event);
        });
    }

    /**
     * @public
     * @param {Data} data 
     */
    refresh(data) {
        this.data = data;
        this.widgetParameters = {};
        const descendantPromise = this.refreshDescendantHandlers(data); // attributes might have variables !
        this.targetPageUrl = this.tag.getAttribute('xis:page');
        this.targetWidgetUrl = this.tag.getAttribute('xis:widget');
        this.targetContainerId = this.tag.getAttribute('xis:target-container');
        return descendantPromise;
    }
    
    /**
     * 
     * @param {String} name 
     * @param {String} value 
     */
    addParameter(name, value) {
        this.widgetParameters[name] = value;
    }


    /**
     * @public
     * @param {Event} e 
     * @returns {Promise<void>} 
     */
    onClick(e) {
        return new Promise((resolve, _) => {
            var container = this.getTargetContainer();
            var handler = app.tagHandlers.getHandler(container);
            if (!handler) {
                var containerId = this.targetContainerId || 'parent container';
                throw new Error('No widget container handler found for "' + containerId + '". This should not happen - please report this as a bug.');
            }
            var widgetParametersInUrl = urlParameters(this.targetWidgetUrl);
            for (var key of Object.keys(widgetParametersInUrl)) {
                this.widgetParameters[key] = widgetParametersInUrl[key];
            }
            var widgetState = new WidgetState(app.pageController.resolvedURL, this.widgetParameters);
            var widgetId = stripQuery(this.targetWidgetUrl);
            handler.initBuffer()
                .then(() => handler.showWidget(widgetId, widgetState))
                .then(() => handler.commitBuffer())
                .then(() => resolve());
        });
    }

    /**
     * @private
     * @returns {Element}
     */
    getTargetContainer() {
        if (this.targetContainerId) {
            var container = this.widgetContainers.findContainer(this.targetContainerId);
            if (!container) {
                throw new Error('Widget container with id "' + this.targetContainerId + '" not found. Make sure a widget-container with this container-id exists on the page.');
            }
            return container;
        }
        var parentContainer = this.findParentWidgetContainer();
        if (!parentContainer) {
            throw new Error('No target container specified and no parent widget container found. Either add xis:target-container="containerId" to the link or place it inside a widget-container.');
        }
        return parentContainer;
    }




    asString() {
        return 'Link';
        // TODO
    }


}


