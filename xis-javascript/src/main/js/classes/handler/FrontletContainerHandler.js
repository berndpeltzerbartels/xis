/**
 * @class FrontletContainerHandler
 * @extends {TagHandler}
 * @package classes/handler
 * @access public
 * @description This handler is responsible for handling a tag for presenting frontlets.
 * 
 * @property {Frontlets} frontlets
 * @property {FrontletContainers} frontletContainers
 * @property {TagHandlers} tagHandlers
 * @property {FrontletInstance} frontletInstance
 * @property {String} containerId
 * @property {Expression} containerIdExpression
 * @property {Expression} defaultFrontletExpression
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
        this.defaultFrontletExpression = this.variableTextContentFromAttribute('default-frontlet')
                || this.variableTextContentFromAttribute('default-frontlet');
        this.type = 'frontlet-container-handler';
        this.scrollToTop = tag.getAttribute('scroll-to-top') === 'true';
        this.buffer = undefined;
        this.frontletParameters = {};
        this.collectingDefaultParameters = false;
        this.defaultFrontletActive = false;
    }

    /**
     * @public
     * @param {String} name 
     * @param {String} value 
     */
    addParameter(name, value) {
       if (this.collectingDefaultParameters) {
           this.frontletParameters[name] = value;
       }
    }

    /**
     * Checks if the response should be handled by a different container.
     * @private
     * @param {ServerResponse} response
     * @returns {boolean}
     */
    shouldDelegateToTargetContainer(response) {
        return response.frontletContainerId &&
               response.frontletContainerId !== this.containerId;
    }

    /**
    * @public
    * @param {ServerResponse} response 
    * @returns {Promise<void>}
    */
    handleActionResponse(response) {
        if (this.shouldDelegateToTargetContainer(response)) {
            var targetContainer = app.frontletContainers.get(response.frontletContainerId);
            if (targetContainer) {
                return targetContainer.handleActionResponse(response);
            }
            console.warn('Target container not found:', response.frontletContainerId);
        }
        var data = response.data;
        updateStores(response);
        this.refreshContainerId(data);

        const nextFrontletId = response.nextFrontletId ? app.client.config.getFrontletId(response.nextFrontletId) : undefined;
        const frontletChanges = !!nextFrontletId && nextFrontletId !== this.currentFrontletId();
        this.frontletParameters = frontletChanges
            ? this.parametersForNewFrontlet(response)
            : mergeObjects(this.frontletParameters, response.frontletParameters);
        if (!this.frontletState) {
            this.frontletState = new FrontletState(app.pageController.resolvedURL, this.frontletParameters);
        }
        this.frontletState.frontletParameters = this.frontletParameters;
        data.setValue(['frontletParameters'], this.frontletParameters);

        return PageController.enqueue(() => {
            if (frontletChanges) {
                // Frontlet-Wechsel: Buffer verwenden damit Container nicht kurz leer erscheint
                return this.initBuffer()
                    .then(() => this.bindNextFrontletIfNeeded(nextFrontletId))
                    .then(() => this.refreshDescendantHandlers(data))
                    .then(() => this.updatePageMetadata(response))
                    .then(() => app.frontletContainers.handleReloadFrontlets(response.reloadFrontlets))
                    .then(() => app.pageController.handleUpdateEventsNow(response.updateEventKeys))
                    .then(pageUpdated => this.handleFrontletContainerUpdates(pageUpdated, response.updateEventKeys))
                    .then(() => this.commitBuffer());
            } else {
                // Kein Frontlet-Wechsel: in-place refresh, kein DOM-Flackern
                return this.refreshDescendantHandlers(data)
                    .then(() => this.updatePageMetadata(response))
                    .then(() => app.frontletContainers.handleReloadFrontlets(response.reloadFrontlets))
                    .then(() => app.pageController.handleUpdateEventsNow(response.updateEventKeys))
                    .then(pageUpdated => this.handleFrontletContainerUpdates(pageUpdated, response.updateEventKeys));
            }
        });
    }

    /**
     * @public
     * @param {Data} data
     */
    refresh(data) {
        this.data = data;
        const hasBoundFrontlet = !!this.frontletInstance;
        const currentFrontletParameters = this.frontletParameters || {};
        this.frontletParameters = {};
        this.refreshContainerId(data);
        if (this.isInitialDefaultFrontletLoad()) {
            return this.refreshInitialDefaultFrontlet(data);
        }
        if (!this.bindByFrontletAnnotation()) {
            this.bindFrontletInitial(data);
        }
        this.frontletParameters = this.parametersForRefresh(hasBoundFrontlet, currentFrontletParameters, data);
        this.frontletState = new FrontletState(app.pageController.resolvedURL, this.frontletParameters);
        data.setValue(['frontletParameters'], this.frontletParameters);
        const descendantPromise = this.refreshDescendantHandlers(data); // xis:parameter tags will call addParameter
        var promises = [];
        if (this.frontletInstance) {
            promises.push(this.reloadDataAndRefresh(data));
        }
        return Promise.all(promises.concat([descendantPromise]));
    }

    isInitialDefaultFrontletLoad() {
        return !!this.defaultFrontletExpression && !this.frontletInstance;
    }

    parametersForRefresh(hasBoundFrontlet, currentFrontletParameters, data) {
        if (hasBoundFrontlet && this.defaultFrontletActive) {
            return this.parametersForBoundDefaultFrontlet(data, currentFrontletParameters);
        }
        if (hasBoundFrontlet) {
            // Explicitly opened frontlets own their parameter scope. Reusing parent
            // parameters here would overwrite link/action parameters on every refresh.
            return currentFrontletParameters;
        }
        return mergeObjects(this.frontletParameters, data.getValue(['frontletParameters']));
    }

    parametersForBoundDefaultFrontlet(data, currentFrontletParameters) {
        var defaultFrontletUrl = this.defaultFrontletUrl(data);
        if (!defaultFrontletUrl) {
            return currentFrontletParameters;
        }
        // A bound default frontlet still follows the container's default-frontlet
        // expression. If the parent model changes that expression, the next model
        // request must use the newly evaluated query parameters.
        this.ensureFrontletBound(app.client.config.getFrontletId(defaultFrontletUrl));
        return urlParameters(defaultFrontletUrl);
    }

    refreshInitialDefaultFrontlet(data) {
        this.frontletParameters = mergeObjects(this.frontletParameters, data.getValue(['frontletParameters']));
        if (!this.bindByFrontletAnnotation()) {
            this.bindFrontletInitial(data);
        }
        return this.refreshContainerParameterHandlers(data)
            .then(() => {
                // The default frontlet is already bound, but its first model request has
                // not happened yet. Container-level parameters must now override any
                // inherited page/frontlet parameters, otherwise two default containers on
                // the same page can accidentally reuse each other's parameter values.
                this.frontletState = new FrontletState(app.pageController.resolvedURL, this.frontletParameters);
                data.setValue(['frontletParameters'], this.frontletParameters);
                return this.frontletInstance ? this.reloadDataAndRefresh(data) : Promise.resolve();
            });
    }

    refreshContainerParameterHandlers(data) {
        // Container-level <xis:parameter> tags configure only the default frontlet.
        // Once a different frontlet is opened explicitly, these values must no
        // longer be applied to the container. Otherwise stale default parameters can
        // override the parameters of the newly opened frontlet.
        var parameterHandlers = this.descendantHandlers.filter(handler => handler.type === 'parameter-handler');
        this.collectingDefaultParameters = true;
        return Promise.all(parameterHandlers.map(handler => handler.refresh(data)))
            .finally(() => this.collectingDefaultParameters = false);
    }

    handleUpdateEvent() {
        return PageController.enqueue(() => this.refresh(this.data));
    }

    /**
    /**
     * @public
     * @param {string} frontletId
     * @param {FrontletState} frontletState
     * @returns {Promise<void>}
     */
    showFrontlet(frontletId, frontletState) {
        this.frontletState = frontletState;
        this.frontletParameters = frontletState.frontletParameters || {};
        this.defaultFrontletActive = false;
        this.ensureFrontletBound(frontletId, true);
        this.frontletState = frontletState;
        return PageController.enqueue(() => this.reloadDataAndRefresh(this.parentData()));
    }

    changesFrontlet(frontletId) {
        return frontletId && frontletId !== this.currentFrontletId();
    }
    /**
     * @public
     * @returns {Promise<void>}
     */
    initBuffer() {
        return new Promise((resolve, _) => {
            // Keep the current frontlet visible while the next one is built off-DOM.
            // commitBuffer() performs the visible replacement only after refresh finished.
            this.buffer = document.createDocumentFragment();
            resolve();
        });
    }

    /**
     * @public
     * @returns {Promise<void>}
     */
    commitBuffer() {
        return new Promise((resolve, _) => {
            while (this.tag.firstChild) {
                this.tag.removeChild(this.tag.firstChild);
            }
            while (this.buffer.firstChild) {
                this.tag.appendChild(this.buffer.firstChild);
            }
            this.buffer = undefined;
            resolve();
        });
    }


    currentFrontletId() {
        return this.frontletInstance ? this.frontletInstance.frontlet.id : undefined;
    }

    currentParameters() {
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
     * Binds the initial frontlet to this container on first load.
     * @private
     * @param {Data} parentData
     */
    bindFrontletInitial(parentData) {
        if (this.frontletInstance) {
            return; // Already bound, only run once
        }
        this.bindDefaultFrontlet(parentData);
    }

    bindByFrontletAnnotation() {
        var response = app.currentResponse;
        for (var defaultFrontlet of response.defaultFrontlets) {
            if (defaultFrontlet.containerId === this.containerId) {
                this.ensureFrontletBound(defaultFrontlet.frontletId);
                this.frontletState = new FrontletState(app.pageController.resolvedURL, this.frontletParameters);
                this.defaultFrontletActive = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Binds the default frontlet specified by xis:default-frontlet attribute in HTML.
     * @private
     * @param {Data} parentData
     */
    bindDefaultFrontlet(parentData) {
        if (!this.defaultFrontletExpression) {
            return;
        }

        var frontletUrl = this.defaultFrontletUrl(parentData);
        if (!frontletUrl) {
            return;
        }

        // Extract and merge URL parameters with xis:parameter tag parameters
        var frontletParametersInUrl = urlParameters(frontletUrl);
        for (var key of Object.keys(frontletParametersInUrl)) {
            this.frontletParameters[key] = frontletParametersInUrl[key];
        }

        var frontletId = app.client.config.getFrontletId(frontletUrl);
        this.ensureFrontletBound(frontletId);
        this.frontletState = new FrontletState(app.pageController.resolvedURL, this.frontletParameters);
        this.defaultFrontletActive = true;
    }

    defaultFrontletUrl(parentData) {
        return this.defaultFrontletExpression ? this.defaultFrontletExpression.evaluate(parentData) : undefined;
    }

    /**
     * @private
     * @param {string} frontletId
     * @param {boolean} shouldScroll - whether to scroll after binding frontlet
     */
    ensureFrontletBound(frontletId, shouldScroll) {
        if (shouldScroll === undefined) {
            shouldScroll = false;
        }
        if (this.frontletInstance) {
            if (this.frontletInstance.frontlet.id == frontletId) {
                return;
            } else {
                var parent = this.frontletInstance.root.parentNode;
                // During a buffered switch the old root stays in the visible container
                // until commitBuffer(); only roots already attached to the buffer are removed here.
                if (parent == this.buffer || (!this.buffer && parent == this.tag)) {
                    parent.removeChild(this.frontletInstance.root);
                }
                this.removeDescendantHandler(this.frontletInstance.rootHandler);
                this.frontletInstance.dispose();
            }
        }
        this.frontletInstance = assertNotNull(this.frontlets.getFrontletInstance(frontletId), 'no such frontlet: ' + frontletId);
        this.frontletInstance.containerHandler = this;
        var frontletRoot = assertNotNull(this.frontletInstance.root, 'no frontlet root: ' + frontletId);
        var target = this.buffer ? this.buffer : this.tag;
        target.appendChild(frontletRoot);
        var frontletHandler = assertNotNull(this.frontletInstance.rootHandler, 'no frontlet handler: ' + frontletId);
        if (shouldScroll && this.scrollToTop) {
            window.scrollTo(0, 0);
        }
        this.addDescendantHandler(frontletHandler);
    }
    /**
     * @private
     * @param {string} nextFrontletId
     */
    bindNextFrontletIfNeeded(nextFrontletId) {
        if (nextFrontletId) {
            this.defaultFrontletActive = false;
            this.ensureFrontletBound(nextFrontletId, true);
        }
    }

    parametersForNewFrontlet(response) {
        // A frontlet switch starts a new parameter scope. Parameters declared on the
        // container belong only to its default frontlet; reusing them here would let
        // stale default values override the explicitly opened frontlet.
        return mergeObjects(response.frontletParameters, urlParameters(response.nextFrontletId));
    }

    /**
     * @private
     * @param {ServerResponse} response
     */
    updatePageMetadata(response) {
        if (response.annotatedTitle) {
            app.pageController.setTitle(response.annotatedTitle);
        }
        return response;
    }

    /**
     * @private
     * @param {boolean} pageUpdated
     * @param {Array} updateEventKeys
     * @returns {Promise<void>}
     */
    handleFrontletContainerUpdates(pageUpdated, updateEventKeys) {
        if (pageUpdated) {
            return Promise.resolve();
        }
        return app.frontletContainers.handleUpdateEventsNow(updateEventKeys);
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
    updateFrontletStateData(data) {
        this.frontletState.data = data;
        return data;
    }

    /**
     * @private
     * @param {Data} data
     * @returns {Data}
     */
    mergeParameters(data) {
        this.frontletParameters = mergeObjects(this.frontletParameters, data.getValue(['frontletParameters']));
        this.frontletState.frontletParameters = this.frontletParameters;
        data.setValue(['frontletParameters'], this.frontletParameters);
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
            return app.client.loadFrontletData(this.frontletInstance, this.frontletState, this)
                .then(response => this.updatePageMetadata(response))
                .then(response => {
                    updateStores(response);
                    return response;
                })
                .then(response => this.enrichResponseDataWithUrlInfo(response))
                .then(data => this.attachParentData(data, parentData))
                .then(data => this.updateFrontletStateData(data))
                .then(data => this.mergeParameters(data))
                .then(data => this.refreshDescendantHandlers(data).then(() => data))
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
