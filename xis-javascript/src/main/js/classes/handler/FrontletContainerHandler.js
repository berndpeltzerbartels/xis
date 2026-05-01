/**
 * @class FrontletContainerHandler
 * @extends {TagHandler}
 * @package classes/handler
 * @access public
 * @description This handler is responsible for handling a tag for presenting widgets.
 * 
 * @property {Frontlets} frontlets
 * @property {FrontletContainers} frontletContainers
 * @property {TagHandlers} tagHandlers
 * @property {FrontletInstance} frontletInstance
 * @property {String} containerId
 * @property {Expression} containerIdExpression
 * @property {Expression} defaultWidgetExpression
 * @property {String} type
 */
class FrontletContainerHandler extends TagHandler {

    /**
     *
     * @param {Element} tag
     * @param {Frontlets} frontlets
     * @param {FrontletContainers} frontletContainers
     */
    constructor(tag, frontlets, frontletContainers, tagHandlers) {
        super(tag);
        this.frontlets = frontlets;
        this.frontletContainers = frontletContainers;
        this.tagHandlers = tagHandlers;
        this.frontletInstance = undefined;
        this.containerId = undefined;
        this.containerIdExpression = this.variableTextContentFromAttribute('container-id');
        this.defaultWidgetExpression = this.variableTextContentFromAttribute('default-widget');
        this.type = 'widget-container-handler';
        this.scrollToTop = tag.getAttribute('scroll-to-top') === 'true';
        this.buffer = undefined;
        this.frontletParameters = {};
    }

    /**
     * @public
     * @param {String} name 
     * @param {String} value 
     */
    addParameter(name, value) {
       this.frontletParameters[name] = value;
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
            var targetContainer = app.frontletContainers.get(response.widgetContainerId);
            if (targetContainer) {
                return targetContainer.handleActionResponse(response);
            }
            console.warn('Target container not found:', response.widgetContainerId);
        }
        this.frontletParameters = mergeObjects(this.frontletParameters, response.widgetParameters);
        if (!this.frontletState) {
            this.frontletState = new FrontletState(app.pageController.resolvedURL, this.frontletParameters);
        }
        var data = response.data;
        data.setValue(['widgetParameters'], this.frontletParameters);
        this.refreshContainerId(data);

        const frontletChanges = !!response.nextWidgetId && response.nextWidgetId !== this.currentWidgetId();

        return PageController.enqueue(() => {
            if (frontletChanges) {
                // Widget-Wechsel: Buffer verwenden damit Container nicht kurz leer erscheint
                return this.initBuffer()
                    .then(() => this.bindNextWidgetIfNeeded(response))
                    .then(() => this.refreshDescendantHandlers(data))
                    .then(() => this.updatePageMetadata(response))
                    .then(() => app.pageController.handleUpdateEvents(response.updateEventKeys))
                    .then(pageUpdated => this.handleWidgetContainerUpdates(pageUpdated, response.updateEventKeys))
                    .then(() => updateStores(response))
                    .then(() => this.commitBuffer());
            } else {
                // Kein Widget-Wechsel: in-place refresh, kein DOM-Flackern
                return this.refreshDescendantHandlers(data)
                    .then(() => this.updatePageMetadata(response))
                    .then(() => app.pageController.handleUpdateEvents(response.updateEventKeys))
                    .then(pageUpdated => this.handleWidgetContainerUpdates(pageUpdated, response.updateEventKeys))
                    .then(() => updateStores(response));
            }
        });
    }

    /**
     * @public
     * @param {Data} data
     */
    refresh(data) {
        this.data = data;
        this.frontletParameters = {};
        this.refreshContainerId(data);
        if (!this.bindByWidgetAnnotation()) {
            this.bindWidgetInitial(data);
        }
        this.frontletParameters = mergeObjects(this.frontletParameters, data.getValue(['widgetParameters']));
        this.frontletState = new FrontletState(app.pageController.resolvedURL, this.frontletParameters);
        data.setValue(['widgetParameters'], this.frontletParameters);
        const descendantPromise = this.refreshDescendantHandlers(data); // xis:parameter tags will call addParameter
        var promises = [];
        if (this.frontletInstance) {
            promises.push(this.reloadDataAndRefresh(data));
        }
        return Promise.all(promises.concat([descendantPromise]));
    }

    handleUpdateEvent() {
        return PageController.enqueue(() => this.refresh(this.data));
    }

    /**
    /**
     * @public
     * @param {string} widgetId 
     * @param {FrontletState} frontletState
     * @returns {Promise<void>}
     */
    showWidget(widgetId, frontletState) {
        this.frontletState = frontletState;
        this.ensureWidgetBound(widgetId, true);
        this.frontletState = frontletState;
        return PageController.enqueue(() => this.reloadDataAndRefresh(this.parentData()));
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
        return this.frontletInstance ? this.frontletInstance.frontlet.id : undefined;
    }

    currentWidgetParameters() {
        return this.frontletParameters;
    }


    /**
     * @private
     * @param {Data} parentData
     */
    refreshContainerId(parentData) {
        if (this.containerIdExpression) {
            var containerId = this.containerIdExpression.evaluate(parentData);
            if (this.containerId) {
                this.frontletContainers.updateContainerId(this.containerId, containerId);
            } else {
                this.frontletContainers.addContainer(this.tag, containerId);
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
        if (this.frontletInstance) {
            return; // Already bound, only run once
        }
        this.bindDefaultWidget(parentData);
    }

    bindByWidgetAnnotation() {
        var response = app.currentResponse;
        for (var defaultFrontlet of response.defaultWidgets) {
            if (defaultFrontlet.containerId === this.containerId) {
                this.ensureWidgetBound(defaultFrontlet.widgetId);
                this.frontletState = new FrontletState(app.pageController.resolvedURL, this.frontletParameters);
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

        var frontletUrl = this.defaultWidgetExpression.evaluate(parentData);
        if (!frontletUrl) {
            return;
        }

        // Extract and merge URL parameters with xis:parameter tag parameters
        var frontletParametersInUrl = urlParameters(frontletUrl);
        for (var key of Object.keys(frontletParametersInUrl)) {
            this.frontletParameters[key] = frontletParametersInUrl[key];
        }

        var widgetId = stripQuery(frontletUrl);
        this.ensureWidgetBound(widgetId);
        this.frontletState = new FrontletState(app.pageController.resolvedURL, this.frontletParameters);
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
        if (this.frontletInstance) {
            if (this.frontletInstance.frontlet.id == widgetId) {
                return;
            } else {
                var parent = this.frontletInstance.root.parentNode;
                if (parent == this.tag || parent == this.buffer) {
                    parent.removeChild(this.frontletInstance.root);
                }
                this.removeDescendantHandler(this.frontletInstance.rootHandler);
                this.frontletInstance.dispose();
            }
        }
        this.frontletInstance = assertNotNull(this.frontlets.getWidgetInstance(widgetId), 'no such widget: ' + widgetId);
        this.frontletInstance.containerHandler = this;
        var frontletRoot = assertNotNull(this.frontletInstance.root, 'no widget root: ' + widgetId);
        var target = this.buffer ? this.buffer : this.tag;
        target.appendChild(frontletRoot);
        var frontletHandler = assertNotNull(this.frontletInstance.rootHandler, 'no widget handler: ' + widgetId);
        if (shouldScroll && this.scrollToTop) {
            window.scrollTo(0, 0);
        }
        this.addDescendantHandler(frontletHandler);
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
        return app.frontletContainers.handleUpdateEvents(updateEventKeys);
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
        this.frontletState.data = data;
        return data;
    }

    /**
     * @private
     * @param {Data} data
     * @returns {Data}
     */
    mergeWidgetParameters(data) {
        this.frontletParameters = mergeObjects(this.frontletParameters, data.getValue(['widgetParameters']));
        this.frontletState.frontletParameters = this.frontletParameters;
        data.setValue(['widgetParameters'], this.frontletParameters);
        return data;
    }

    /**
     * @private
     */
    reloadDataAndRefresh(parentData) {
        return this.doLoad(parentData);
    }

    doLoad(parentData) {
        if (this.frontletInstance) {
            const response = app.currentResponse;
            return app.client.loadWidgetData(this.frontletInstance, this.frontletState, this)
                .then(response => this.updatePageMetadata(response))
                .then(response => this.enrichResponseDataWithUrlInfo(response))
                .then(data => this.attachParentData(data, parentData))
                .then(data => this.updateWidgetStateData(data))
                .then(data => this.mergeWidgetParameters(data))
                .then(data => this.refreshDescendantHandlers(data).then(() => data))
                .then(() => updateStores(response))
                .catch(e => reportError(e));
        }
        return Promise.resolve();
    }

    /**
     * Enriches response data with current URL information.
     * @private
     * @param {ServerResponse} response
     * @returns {Data}
     */
    enrichResponseDataWithUrlInfo(response) {
        const data = response.data;
        if (this.frontletState && this.frontletState.resolvedURL) {
            const url = this.frontletState.resolvedURL.url;
            const pathname = url.split('?')[0]; // Remove query string
            data.setValue(['url'], url);
            data.setValue(['pathname'], pathname);
            data.setValue(['queryParams'], this.frontletState.resolvedURL.urlParameters);
        }
        return data;
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
