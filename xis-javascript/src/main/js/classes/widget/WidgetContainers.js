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
     * @param {string} widgetId 
     * @returns {WidgetContainerHandler}  
     */
    findContainerHandlerByWidgetId(widgetId) {
        for (var key in this.containers) {
            var container = this.containers[key];
            if (container._handler.widgetInstance.id === widgetId) {
                return container_handler;
            }
        }
        return undefined;
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