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
// TODO Widget-RootHandler darf kein descendant handler sein, sondern muss in WidgetContainerHandler integriert werden.
// Die Descendanthandler werden eventuell entfernt. Die WidgetInstance ist besser geeignet.
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
    }

    /**
    * @public
    * @param {ServerResponse} response 
    */
    handleActionResponse(response) {
        app.backendService.triggerAdditionalReloadsOnDemand(response); // TODO move it
        
        // Trigger reactive state updates with this WidgetContainerHandler as the invoker
        // This ensures the anti-recursion logic stops at this level
        app.backendService.triggerReactiveStateUpdates(response, this);
        
        if (response.nextURL) {
            app.pageController.handleActionResponse(response);
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
       // app.eventPublisher.publish(EventType.REQUEST_COMPLETED);
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
     * State refresh - updates widget with current state data without reloading from backend.
     * This prevents infinite recursion during reactive state updates.
     * @public
     * @param {Data} data - Current state data
     * @param {TagHandler} invoker - The handler that initiated the state refresh
     */
    stateRefresh(data, invoker) {
        // Anti-recursion: Skip if we triggered the state refresh
        if (this === invoker) {
            return;
        }
        
        // Only refresh existing widget with current data, don't reload from backend
        if (this.widgetInstance && this.widgetState && this.widgetState.data) {
            // No need to load state data into Data object anymore  
            // ClientStateVariable and LocalStoreVariable access stores directly
            
            // Update the widget's data with new state values
            this.widgetState.data = data;
            // Refresh descendant handlers with the new state data
            this.refreshDescendantHandlers(data);
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
      //  app.eventPublisher.publish(EventType.REQUEST_COMPLETED);
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
                this.tag.removeChild(this.widgetInstance.root);
                this.removeDescendantHandler(this.widgetInstance.rootHandler);
                this.widgetInstance.dispose();
            }
        }
        this.widgetInstance = assertNotNull(this.widgets.getWidgetInstance(widgetId), 'no such widget: ' + widgetId);
        var widgetRoot = assertNotNull(this.widgetInstance.root, 'no widget root: ' + widgetId);
        this.tag.appendChild(widgetRoot);
        var widgetHandler = assertNotNull(this.widgetInstance.rootHandler, 'no widget handler: ' + widgetId);   
        this.addDescendantHandler(widgetHandler);
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
            app.backendService.loadWidgetData(this.widgetInstance, this.widgetState, this)
                .then(data => { data.parentData = parentData; data.scope = scope; return data; })
                .then(data => { console.log("data"+(typeof data)); data.parentData = parentData; data.scope = scope; return data; })
                .then(data => { this.widgetState.data = data; return data; })
                .then(data => this.refreshDescendantHandlers(data))
                .then(data => { app.eventPublisher.publish(EventType.WIDGET_LOADED, { widget: this.widgetInstance, data }); return data; })
                .catch(e => reportError(e));
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