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
        this.widgetParameters = {};
        this.containerId = undefined;
        this.containerIdExpression = this.variableTextContentFromAttribute('container-id');
        this.defaultWidgetExpression = this.variableTextContentFromAttribute('default-widget');
        this.type = 'widget-container-handler';
        this.tagContentSetter = new TagContentSetter();
        this.scrollToTop = tag.getAttribute('scroll-to-top') === 'true';
        this.buffer = undefined;
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
        // If response specifies a different container via annotation, delegate to that container
        if (this.shouldDelegateToTargetContainer(response)) {
            var targetContainer = app.widgetContainers.get(response.widgetContainerId);
            if (targetContainer) {
                return targetContainer.handleActionResponse(response);
            }
            console.warn('Target container not found:', response.widgetContainerId);
        }

        if (!this.widgetState) {
            this.widgetState = new WidgetState(app.pageController.resolvedURL, {});
        }
        var data = response.data;
        this.refreshContainerId(data);
        return this.initBuffer()
            .then(() => {
                if (response.nextWidgetId) {
                    this.ensureWidgetBound(response.nextWidgetId, true);
                }
                return this.refreshDescendantHandlers(data);
            })
            .then(() => {
                if (response.annotatedTitle) {
                    app.pageController.setTitle(response.annotatedTitle);
                }
                if (response.annotatedAddress) {
                    app.pageController.setAddress(response.annotatedAddress);
                }
            })
            .then(() => app.pageController.handleUpdateEvents(response.updateEventKeys))
            .then(pageUpdated => pageUpdated ? Promise.resolve() : app.widgetContainers.handleUpdateEvents(response.updateEventKeys))
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
        const descendantPromise = this.refreshDescendantHandlers(data); // xis:parameter tags will call addParameter
        this.bindWidgetInitial(data);
        var widgetParameters = this.widgetState ? this.widgetState.widgetParameters : {};
        this.widgetState = new WidgetState(app.pageController.resolvedURL, widgetParameters);
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
     * Binds the initial widget to this container on first load.
     * Checks annotation-based containerId first (deep linking), then falls back to default-widget attribute.
     * @private
     * @param {Data} parentData
     */
    bindWidgetInitial(parentData) {
        if (this.widgetInstance) {
            return; // Already bound, only run once
        }

        // Priority 1: Annotation-based widget assignment (for deep linking)
        if (this.bindAnnotatedWidget()) {
            return;
        }

        // Priority 2: Default widget from HTML attribute
        this.bindDefaultWidget(parentData);
    }

    /**
     * Attempts to bind a widget specified by @Widget(containerId) annotation.
     * This enables deep linking where the widget's annotation determines the target container.
     * @private
     * @returns {boolean} true if widget was bound, false otherwise
     */
    bindAnnotatedWidget() {
        var response = app.pageController.currentResponse;
        if (!response || !response.nextWidgetId || response.widgetContainerId !== this.containerId) {
            return false;
        }

        this.ensureWidgetBound(response.nextWidgetId);
        this.widgetState = new WidgetState(app.pageController.resolvedURL, this.widgetParameters);
        return true;
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
     */
    reloadDataAndRefresh(parentData) {
        return this.doLoad(parentData);
    }

    doLoad(parentData) {
        if (this.widgetInstance) {
            return app.client.loadWidgetData(this.widgetInstance, this.widgetState, this)
                .then(response => {
                    if (response.annotatedTitle) {
                        app.pageController.setTitle(response.annotatedTitle);
                    }
                    if (response.annotatedAddress) {
                        app.pageController.setAddress(response.annotatedAddress);
                    }
                    return response.data;
                })
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