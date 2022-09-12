class XISContainers {

    constructor() {
        this.container = {};
    }

    addContainer(container) {
        return this.containers[container.containerId] = container;
    }

    getContainer(containerId) {
        return this.containers[containerId];
    }

    
}
