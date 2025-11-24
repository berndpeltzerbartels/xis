/**
 * @class WidgetContainerHandler
 * @extends {TagHandler}
 * @package classes/handler
 * @access public
 * @description This handler is responsible for handling a tag for presenting widgets.
 * 
 * @property {Widgets} widgets
 * @property {WidgetContainers} widgetContainers
 * @property {TagHandlers} tagHandlers
 * @property {WidgetInstance} widgetInstance
 * @property {String} containerId
 * @property {Expression} containerIdExpression
 * @property {Expression} defaultWidgetExpression
 * @property {String} type
 */
class WidgetContainerHandler extends TagHandler {

    /**
     *
     * @param {Element} tag
     * @param {Widgets} widgets
     * @param {WidgetContainers} widgetContainers
     */
    constructor(tag, widgets, widgetContainers, tagHandlers) {
        super(tag);
        this.widgets = widgets;
        this.widgetContainers = widgetContainers;
        this.tagHandlers = tagHandlers;
        this.widgetInstance = undefined;
        this.containerId = undefined;
        this.containerIdExpression = this.variableTextContentFromAttribute('container-id');
        this.defaultWidgetExpression = this.variableTextContentFromAttribute('default-widget');
        this.type = 'widget-container-handler';
        this.tagContentSetter = new TagContentSetter();
    }

    /**
    * @public
    * @param {ServerResponse} response 
    * @returns {Promise<void>}
    */
    handleActionResponse(response) {
        if (response.nextWidgetId) {
            this.ensureWidgetBound(response.nextWidgetId);
        }
        if (!this.widgetState) {
            this.widgetState = new WidgetState(app.pageController.resolvedURL, {});
        }
        var data = response.data;
        this.refreshContainerId(data);
        return app.pageController.initBuffer()
            .then(() => this.refreshDescendantHandlers(data))
            .then(() => app.pageController.commitBuffer());

    }

    /**
     * @public
     * @param {Data} data
     */
    refresh(data) {
        this.data = data;
        this.refreshContainerId(data);
        this.bindDefaultWidgetInitial(data);
        var widgetParameters = this.widgetState ? this.widgetState.widgetParameters : {};
        this.widgetState = new WidgetState(app.pageController.resolvedURL, widgetParameters);
        var promises = [];
        if (this.widgetInstance) {
            promises.push(this.reloadDataAndRefresh(data));
        }
        return Promise.all(promises.concat([this.refreshDescendantHandlers(data)]));
    }

    handleUpdateEvent() {
        this.refresh(this.data);
    }

    /**
     * @public
     * @param {string} widgetId 
     * @param {WidgetState} widgetState
     * @returns {Promise<void>}
     */
    showWidget(widgetId, widgetState) {
        this.widgetState = widgetState;
        this.ensureWidgetBound(widgetId);
        this.widgetState = widgetState;
        this.reloadDataAndRefresh(this.parentData());
    }

    /**
     * @private
     */
    currentWidgetId() {
        return this.widgetInstance ? this.widgetInstance.widget.id : undefined;
    }


    /**
     * @private
     * @param {Data} parentData
     */
    refreshContainerId(parentData) {
        if (this.containerIdExpression) {
            var containerId = this.containerIdExpression.evaluate(parentData);
            if (this.containerId) {
                this.widgetContainers.updateContainerId(this.containerId, containerId);
            } else {
                this.widgetContainers.addContainer(this.tag, containerId);
            }
            this.containerId = containerId;
        }
    }

    /**
     * @private
     * @param {Data} parentData
     */
    bindDefaultWidgetInitial(parentData) {
        if (this.defaultWidgetExpression && !this.widgetInstance) { // once, only
            var widgetUrl = this.defaultWidgetExpression.evaluate(parentData);
            var widgetParameters = urlParameters(widgetUrl);
            var widgetId = stripQuery(widgetUrl);
            this.ensureWidgetBound(widgetId);
            this.widgetState = new WidgetState(app.pageController.resolvedURL, widgetParameters);
        }
    }

    /**
     * @private
     * @param {string} widgetId
     */
    ensureWidgetBound(widgetId) {
        if (this.widgetInstance) {
            if (this.widgetInstance.widget.id == widgetId) {
                return;
            } else {
                if (this.widgetInstance.root.parentNode == this.tag) {
                    this.tag.removeChild(this.widgetInstance.root);
                }
                this.removeDescendantHandler(this.widgetInstance.rootHandler);
                this.widgetInstance.dispose();
            }
        }
        this.widgetInstance = assertNotNull(this.widgets.getWidgetInstance(widgetId), 'no such widget: ' + widgetId);
        this.widgetInstance.containerHandler = this;
        var widgetRoot = assertNotNull(this.widgetInstance.root, 'no widget root: ' + widgetId);
        this.tag.appendChild(widgetRoot);
        var widgetHandler = assertNotNull(this.widgetInstance.rootHandler, 'no widget handler: ' + widgetId);
        this.addDescendantHandler(widgetHandler);
    }

    /**
     * @private
     */
    reloadDataAndRefresh(parentData) {
        return this.doLoad(parentData);
    }

    doLoad(parentData) {
        if (this.widgetInstance) {
            return app.client.loadWidgetData(this.widgetInstance, this.widgetState, this)
                .then(response => response.data)
                .then(data => { data.parentData = parentData; return data; })
                .then(data => { this.widgetState.data = data; return data; })
                .then(data => this.refreshDescendantHandlers(data).then(() => data))
                .then(data => this.tagContentSetter.apply(document, data.idVariables, data.tagVariables))
                .catch(e => reportError(e));
        }
        return Promise.resolve();
    }

    /**
     * @private
     * @returns {Data}
     */
    parentData() {
        var handler = this;
        var parentDataHandler;
        while (handler) {
            if (handler.getData) {
                parentDataHandler = handler;
                break;
            }
            handler = handler.parentHandler;
        }
        return parentDataHandler ? parentDataHandler.getData() : app.pageController.getData();
    }
}