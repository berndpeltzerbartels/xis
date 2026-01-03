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
        this.scrollToTop = tag.getAttribute('scroll-to-top') === 'true';
        this.buffer = undefined;
        this.widgetParameters = {};
    }

    /**
     * @public
     * @param {String} name 
     * @param {String} value 
     */
    addParameter(name, value) {
       this.widgetParameters[name] = value;
    }

    /**
     * Checks if the response should be handled by a different container.
     * @private
     * @param {ServerResponse} response
     * @returns {boolean}
     */
    shouldDelegateToTargetContainer(response) {
        return response.widgetContainerId && 
               response.widgetContainerId !== this.containerId;
    }

    /**
    * @public
    * @param {ServerResponse} response 
    * @returns {Promise<void>}
    */
    handleActionResponse(response) {
        if (this.shouldDelegateToTargetContainer(response)) {
            var targetContainer = app.widgetContainers.get(response.widgetContainerId);
            if (targetContainer) {
                return targetContainer.handleActionResponse(response);
            }
            console.warn('Target container not found:', response.widgetContainerId);
        }
        this.widgetParameters = mergeObjects(this.widgetParameters, response.widgetParameters);
        if (!this.widgetState) {
            this.widgetState = new WidgetState(app.pageController.resolvedURL, this.widgetParameters);
        }
        var data = response.data;
        data.setValue(['widgetParameters'], this.widgetParameters);
        this.refreshContainerId(data);
        return this.initBuffer()
            .then(() => this.bindNextWidgetIfNeeded(response))
            .then(() => this.refreshDescendantHandlers(data))
            .then(() => this.updatePageMetadata(response))
            .then(() => app.pageController.handleUpdateEvents(response.updateEventKeys))
            .then(pageUpdated => this.handleWidgetContainerUpdates(pageUpdated, response.updateEventKeys))
            .then(() => this.commitBuffer());
    }

    /**
     * @public
     * @param {Data} data
     */
    refresh(data) {
        this.data = data;
        this.widgetParameters = {};
        this.refreshContainerId(data);
        if (!this.bindByWidgetAnnotation()) {
            this.bindWidgetInitial(data);
        }
        this.widgetParameters = mergeObjects(this.widgetParameters, data.getValue(['widgetParameters']));
        this.widgetState = new WidgetState(app.pageController.resolvedURL, this.widgetParameters);
        data.setValue(['widgetParameters'], this.widgetParameters);
        debugger;
        const descendantPromise = this.refreshDescendantHandlers(data); // xis:parameter tags will call addParameter
        var promises = [];
        if (this.widgetInstance) {
            promises.push(this.reloadDataAndRefresh(data));
        }
        return Promise.all(promises.concat([descendantPromise]));
    }

    handleUpdateEvent() {
        this.refresh(this.data);
    }

    /**
    /**
     * @public
     * @param {string} widgetId 
     * @param {WidgetState} widgetState
     * @returns {Promise<void>}
     */
    showWidget(widgetId, widgetState) {
        this.widgetState = widgetState;
        this.ensureWidgetBound(widgetId, true);
        this.widgetState = widgetState;
        return this.reloadDataAndRefresh(this.parentData());
    }
    /**
     * @public
     * @returns {Promise<void>}
     */
    initBuffer() {
        return new Promise((resolve, _) => {
            this.buffer = document.createDocumentFragment();
            while (this.tag.firstChild) {
                this.buffer.appendChild(this.tag.firstChild);
            }
            resolve();
        });
    }

    /**
     * @public
     * @returns {Promise<void>}
     */
    commitBuffer() {
        return new Promise((resolve, _) => {
            while (this.buffer.firstChild) {
                this.tag.appendChild(this.buffer.firstChild);
            }
            resolve();
        });
    }


    currentWidgetId() {
        return this.widgetInstance ? this.widgetInstance.widget.id : undefined;
    }

    currentWidgetParameters() {
        return this.widgetParameters;
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
     * Binds the initial widget to this container on first load.
     * @private
     * @param {Data} parentData
     */
    bindWidgetInitial(parentData) {
        if (this.widgetInstance) {
            return; // Already bound, only run once
        }
        this.bindDefaultWidget(parentData);
    }

    bindByWidgetAnnotation() {
        var response = app.pageController.currentResponse;
        for (var defaultWidget of response.defaultWidgets) {
            if (defaultWidget.containerId === this.containerId) {
                this.ensureWidgetBound(defaultWidget.widgetId);
                this.widgetState = new WidgetState(app.pageController.resolvedURL, this.widgetParameters);
                return true;
            }
        }
        return false;
    }

    /**
     * Binds the default widget specified by xis:default-widget attribute in HTML.
     * @private
     * @param {Data} parentData
     */
    bindDefaultWidget(parentData) {
        if (!this.defaultWidgetExpression) {
            return;
        }

        var widgetUrl = this.defaultWidgetExpression.evaluate(parentData);
        if (!widgetUrl) {
            return;
        }

        // Extract and merge URL parameters with xis:parameter tag parameters
        var widgetParametersInUrl = urlParameters(widgetUrl);
        for (var key of Object.keys(widgetParametersInUrl)) {
            this.widgetParameters[key] = widgetParametersInUrl[key];
        }

        var widgetId = stripQuery(widgetUrl);
        this.ensureWidgetBound(widgetId);
        this.widgetState = new WidgetState(app.pageController.resolvedURL, this.widgetParameters);
    }

    /**
     * @private
     * @param {string} widgetId
     * @param {boolean} shouldScroll - whether to scroll after binding widget
     */
    ensureWidgetBound(widgetId, shouldScroll) {
        if (shouldScroll === undefined) {
            shouldScroll = false;
        }
        if (this.widgetInstance) {
            if (this.widgetInstance.widget.id == widgetId) {
                return;
            } else {
                var parent = this.widgetInstance.root.parentNode;
                if (parent == this.tag || parent == this.buffer) {
                    parent.removeChild(this.widgetInstance.root);
                }
                this.removeDescendantHandler(this.widgetInstance.rootHandler);
                this.widgetInstance.dispose();
            }
        }
        this.widgetInstance = assertNotNull(this.widgets.getWidgetInstance(widgetId), 'no such widget: ' + widgetId);
        this.widgetInstance.containerHandler = this;
        var widgetRoot = assertNotNull(this.widgetInstance.root, 'no widget root: ' + widgetId);
        var target = this.buffer ? this.buffer : this.tag;
        target.appendChild(widgetRoot);
        var widgetHandler = assertNotNull(this.widgetInstance.rootHandler, 'no widget handler: ' + widgetId);
        if (shouldScroll && this.scrollToTop) {
            window.scrollTo(0, 0);
        }
        this.addDescendantHandler(widgetHandler);
    }
    /**
     * @private
     * @param {ServerResponse} response
     */
    bindNextWidgetIfNeeded(response) {
        if (response.nextWidgetId) {
            this.ensureWidgetBound(response.nextWidgetId, true);
        }
    }

    /**
     * @private
     * @param {ServerResponse} response
     */
    updatePageMetadata(response) {
        if (response.annotatedTitle) {
            app.pageController.setTitle(response.annotatedTitle);
        }
        if (response.annotatedAddress) {
            app.pageController.setAddress(response.annotatedAddress);
        }
        return response;
    }

    /**
     * @private
     * @param {boolean} pageUpdated
     * @param {Array} updateEventKeys
     * @returns {Promise<void>}
     */
    handleWidgetContainerUpdates(pageUpdated, updateEventKeys) {
        if (pageUpdated) {
            return Promise.resolve();
        }
        return app.widgetContainers.handleUpdateEvents(updateEventKeys);
    }

    /**
     * @private
     * @param {Data} data
     * @param {Data} parentData
     * @returns {Data}
     */
    attachParentData(data, parentData) {
        data.parentData = parentData;
        return data;
    }

    /**
     * @private
     * @param {Data} data
     * @returns {Data}
     */
    updateWidgetStateData(data) {
        this.widgetState.data = data;
        return data;
    }

    /**
     * @private
     * @param {Data} data
     * @returns {Data}
     */
    mergeWidgetParameters(data) {
        this.widgetParameters = mergeObjects(this.widgetParameters, data.getValue(['widgetParameters']));
        this.widgetState.widgetParameters = this.widgetParameters;
        data.setValue(['widgetParameters'], this.widgetParameters);
        return data;
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
                .then(response => this.updatePageMetadata(response))
                .then(response => response.data)
                .then(data => this.attachParentData(data, parentData))
                .then(data => this.updateWidgetStateData(data))
                .then(data => this.mergeWidgetParameters(data))
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