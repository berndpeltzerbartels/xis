/**
 * @class FrontletLinkHandlerBase
 * @extends {TagHandler}
 * @package classes/handler
 * @access public
 * @description Base class for handling links to widgets. Backend is not involved.
 * 
 * @property {String} type
 * @property {FrontletContainers} frontletContainers
 * @property {Object} frontletParameters
 * @property {String} targetPageUrl
 * @property {String} targetFrontletUrl
 * @property {String} targetContainerId
 */
class FrontletLinkHandlerBase extends TagHandler {

    /**
     * 
     * @param {Element} element 
     * @param {FrontletContainers} frontletContainers
     */
    constructor(element, frontletContainers) {
        super(element);
        this.type = 'widget-link-handler-base';
        this.frontletContainers = frontletContainers;
        this.frontletParameters = {};
        this.targetPageUrl = undefined;
        this.targetFrontletUrl = undefined;
        this.targetContainerId = undefined;
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
        this.frontletParameters = {};
        const descendantPromise = this.refreshDescendantHandlers(data); // attributes might have variables !
        this.targetPageUrl = this.tag.getAttribute('xis:page');
        this.targetFrontletUrl = this.tag.getAttribute('xis:widget');
        this.targetContainerId = this.tag.getAttribute('xis:target-container');
        return descendantPromise;
    }
    
    /**
     * 
     * @param {String} name 
     * @param {String} value 
     */
    addParameter(name, value) {
        this.frontletParameters[name] = value;
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
            var frontletParametersInUrl = urlParameters(this.targetFrontletUrl);
            for (var key of Object.keys(frontletParametersInUrl)) {
                this.frontletParameters[key] = frontletParametersInUrl[key];
            }
            var frontletState = new FrontletState(app.pageController.resolvedURL, this.frontletParameters);
            var widgetId = stripQuery(this.targetFrontletUrl);
            handler.initBuffer()
                .then(() => handler.showWidget(widgetId, frontletState))
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
            var container = this.frontletContainers.findContainer(this.targetContainerId);
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
        return 'WidgetLink';
    }


}
