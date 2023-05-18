class WidgetContainers {


    constructor() {
        this.containers = {};
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
     * @param {Element} element 
     * @param {String} id
     */
    addContainer(element, id) {
        this.containers[id] = element;
    }
}