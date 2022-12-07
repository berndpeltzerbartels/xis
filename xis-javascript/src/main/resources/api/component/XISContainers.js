class XISContainers {

    constructor() {
        this.className = 'XISContainers';
        this.container = {};
    }

    addContainer(container) {
        return this.containers[container.containerId] = container;
    }

    getContainer(containerId) {
        return this.containers[containerId];
    }

}
