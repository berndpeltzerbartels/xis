/**
 * @class WidgetContainerHandler
 * @extends {TagHandler}
 * @package classes/handler
 * @access public
 * @description This handler is responsible for handling a tag for presenting widgets.
 * 
 * @property {BackendService} backendService
 * @property {Widgets} widgets
 * @property {WidgetContainers} widgetContainers
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
     * @param {BackendService} backendService
     * @param {Widgets} widgets
     * @param {WidgetContainers} widgetContainers
     */
    constructor(tag, backendService, widgets, widgetContainers) {
        super(tag);
        this.backendService = backendService;
        this.widgets = widgets;
        this.widgetContainers = widgetContainers;
        this.widgetInstance = undefined;
        this.containerId = undefined;
        this.containerIdExpression = this.expressionFromAttribute('container-id');
        this.defaultWidgetExpression = this.expressionFromAttribute('default-widget');
        this.type = 'widget-container-handler';
    }

    /**
    * @public
    * @param {ServerResponse} response 
    */
    handleActionResponse(response) {
        this.backendService.triggerAdditionalReloadsOnDemand(response); // TODO move it
        if (response.nextPageId) {
            app.pageController.handleActionResponse(response.nextPageId, response.nextPageParameters);
        }
        if (response.nextWidgetId) {
            this.ensureWidgetBound(response.nextWidgetId);
        }
        if (!this.widgetState) {
            this.widgetState = new WidgetState(app.pageController.resolvedURL, {});
        }
        var data = response.data;
        this.refreshContainerId(data);
        this.refreshDescendantHandlers(data);
    }

    /**
     * @public
     * @param {Data} data
     */
    refresh(data) {
        this.refreshContainerId(data);
        this.bindDefaultWidgetInitial(data);
        var widgetParameters = this.widgetState ? this.widgetState.widgetParameters : {};
        this.widgetState = new WidgetState(app.pageController.resolvedURL, widgetParameters);
        if (this.widgetInstance) {
            this.reloadDataAndRefresh(data);
        }
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
                this.clearChildren();
                this.widgetInstance.dispose();
                this.descendantHandlers = [];
            }
        }
        this.widgetInstance = assertNotNull(this.widgets.getWidgetInstance(widgetId), 'no such widget: ' + widgetId);
        this.tag.appendChild(this.widgetInstance.root);
        this.addDescendantHandler(this.widgetInstance.root._rootHandler);
    }

    /**
     * @private
     */
    reloadDataAndRefresh(parentData) {
        this.doLoad(parentData, SCOPE_TREE);
    }

    triggerWidgetReload() {
        this.doLoad(new Data({}), SCOPE_CONTROLLER);
    }

    doLoad(parentData, scope) {
        if (this.widgetInstance) {
            var _this = this;
            this.backendService.loadWidgetData(this.widgetInstance, this.widgetState)
                .then(data => { data.parentData = parentData; data.scope = scope; return data; })
                .then(data => { console.log("data"+(typeof data)); data.parentData = parentData; data.scope = scope; return data; })
                .then(data => { _this.widgetState.data = data; return data; })
                .then(data => _this.refreshDescendantHandlers(data))
                .catch(e => console.error(e));
        }
    }

    triggerAdditionalReloads(response) {
       app.backendService.triggerPageReloadOnDemand(response);
       app.backendService.triggerWidgetReloadsOnDemand(response);
       return response;
    }

    /**
     * @private
     * @returns {Data}
     */
    parentData() {
        var e = this.tag.parentNode;
        var parentDataHandler;
        while (e) {
            if (e.handler && e.handler.getData) {
                parentDataHandler = e.handler;
                break;
            }
            e = e.parentNode;
        }
        return parentDataHandler ? parentDataHandler.getData() : app.pageController.getData();
    }
}