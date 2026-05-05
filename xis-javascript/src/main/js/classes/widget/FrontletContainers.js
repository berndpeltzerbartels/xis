class FrontletContainers {


    constructor() {
        this.containers = {};
    }

    updateContainerId(oldId, newId) {
        var container = this.containers[oldId];
        this.containers[oldId] = undefined;
        this.containers[newId] = container;
    }
    /**
     * 
     * @param {String} id
     * @returns {Element} 
     */
    findContainer(id) {
        return this.containers[id];
    }

    /**
     * Get the handler for a widget container by its ID
     * @param {String} id
     * @returns {FrontletContainerHandler}
     */
    get(id) {
        var element = this.containers[id];
        if (!element) {
            return undefined;
        }
        return app.tagHandlers.getHandler(element);
    }

    /**
     * 
     * @param {ClientConfig} config 
     * @returns 
     */
    setConfig(config) {
        this.config = config;
        return Promise.resolve(config);
    }

    handleUpdateEvents(eventIds) {
        const promises = [];
        eventIds.forEach(eventId => {
            const widgetIds = this.widgetIdForUpdateEvent(eventId);
            widgetIds.forEach(widgetId => {
                const handlers = this.findContainerHandlersByWidgetId(widgetId);
                handlers.forEach(handler => promises.push(handler.handleUpdateEvent()));
            });
        });
        return Promise.all(promises);
    }

    handleUpdateEventsNow(eventIds) {
        const promises = [];
        eventIds.forEach(eventId => {
            const widgetIds = this.widgetIdForUpdateEvent(eventId);
            widgetIds.forEach(widgetId => {
                const handlers = this.findContainerHandlersByWidgetId(widgetId);
                handlers.forEach(handler => promises.push(handler.refresh(handler.data)));
            });
        });
        return Promise.all(promises);
    }

    handleReloadFrontlets(frontletIds) {
        if (!frontletIds || frontletIds.length === 0) {
            return Promise.resolve([]);
        }
        const promises = [];
        frontletIds.forEach(frontletId => {
            const handlers = this.findContainerHandlersByWidgetId(frontletId);
            handlers.forEach(handler => promises.push(handler.refresh(handler.data)));
        });
        return Promise.all(promises);
    }


    widgetIdForUpdateEvent(eventId) {
        var widgetIds = [];
        for (var widgetId of Object.keys(this.config.frontletAttributes)) {
            var frontletAttributes = this.config.frontletAttributes[widgetId];
            if (frontletAttributes.updateEventKeys.includes(eventId)) {
                widgetIds.push(widgetId);
            }
        }
        return widgetIds;
    }


    /**
     * 
     * @param {string} widgetId 
     * @returns {FrontletContainerHandler}
     */
    findContainerHandlersByWidgetId(widgetId) {
        const handlers = [];
        for (var key in this.containers) {
            var container = this.containers[key];
            var handler = app.tagHandlers.getHandler(container);
            if (handler.frontletInstance && handler.frontletInstance.frontlet.id === widgetId) {
                handlers.push(handler);
            }
        }
        return handlers;
    }

    /**
     * @param {Element} element 
     * @param {String} id
     */
    addContainer(element, id) {
        this.containers[id] = element;
    }

    reset() {
        this.containers = {};
    }
}
