class WidgetContainers {


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
     * 
     * @param {ClientConfig} config 
     * @returns 
     */
    setConfig(config) {
        this.config = config;
        return config;
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
        for (var widgetId in Object.keys(this.config.widgetAttributes)) {
            var widgetAttributes = this.config.widgetAttributes[widgetId];
            if (widgetAttributes.updateEventKeys.includes(eventId)) {
                widgetIds.push(widgetId);
            }
        }
        return widgetIds;
    }


    /**
     * 
     * @param {string} widgetId 
     * @returns {WidgetContainerHandler}  
     */
    findContainerHandlersByWidgetId(widgetId) {
        const handlers = [];
        for (var key in this.containers) {
            var container = this.containers[key];
            var handler = app.tagHandlers.getHandler(container);
            if (handler.widgetInstance.id === widgetId) {
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