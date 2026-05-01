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
        eventIds.forEach(eventId => {
            const widgetIds = this.widgetIdForUpdateEvent(eventId);
            widgetIds.forEach(widgetId => {
                const handlers = this.findContainerHandlersByWidgetId(widgetId);
                handlers.forEach(handler => handler.handleUpdateEvent());
            });
        });

    }

    widgetIdForUpdateEvent(eventId) {
        var widgetIds = [];
        for (var widgetId of Object.keys(this.config.widgetAttributes)) {
            var frontletAttributes = this.config.widgetAttributes[widgetId];
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
