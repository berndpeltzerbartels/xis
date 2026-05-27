class FrontletContainers {


    constructor() {
        this.containers = {};
        this.anonymousContainers = [];
    }

    updateContainerId(oldId, newId) {
        var entry = this.containers[oldId];
        delete this.containers[oldId];
        this.containers[newId] = entry;
    }
    /**
     * 
     * @param {String} id
     * @returns {Element} 
     */
    findContainer(id) {
        var entry = this.containers[id];
        return entry && entry.active ? entry.element : undefined;
    }

    /**
     * Get the handler for a frontlet container by its ID
     * @param {String} id
     * @returns {FrontletContainerHandler}
     */
    get(id) {
        var element = this.findContainer(id);
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
            const frontletIds = this.frontletIdForUpdateEvent(eventId);
            frontletIds.forEach(frontletId => {
                const handlers = this.findActiveContainerHandlersByFrontletId(frontletId);
                handlers.forEach(handler => promises.push(handler.handleUpdateEvent()));
            });
        });
        return Promise.all(promises);
    }

    handleUpdateEventsNow(eventIds, skipGeneration) {
        const promises = [];
        eventIds.forEach(eventId => {
            const frontletIds = this.frontletIdForUpdateEvent(eventId);
            frontletIds.forEach(frontletId => {
                const handlers = this.findActiveContainerHandlersByFrontletId(frontletId);
                handlers
                    .filter(handler => skipGeneration === undefined || handler.lastModelLoadGeneration !== skipGeneration)
                    .forEach(handler => promises.push(handler.refreshForUpdateEvent()));
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
            const handlers = this.findActiveContainerHandlersByFrontletId(frontletId);
            handlers.forEach(handler => promises.push(handler.refreshForUpdateEvent()));
        });
        return Promise.all(promises);
    }


    frontletIdForUpdateEvent(eventId) {
        var frontletIds = [];
        for (var frontletId of Object.keys(this.config.frontletAttributes)) {
            var frontletAttributes = this.config.frontletAttributes[frontletId];
            if (frontletAttributes.updateEventKeys.includes(eventId)) {
                frontletIds.push(frontletId);
            }
        }
        return frontletIds;
    }


    /**
     * 
     * @param {string} frontletId
     * @returns {FrontletContainerHandler}
     */
    findActiveContainerHandlersByFrontletId(frontletId) {
        const handlers = [];
        for (var key in this.containers) {
            var entry = this.containers[key];
            this.addHandlerIfMatching(handlers, entry, frontletId);
        }
        this.anonymousContainers.forEach(entry => this.addHandlerIfMatching(handlers, entry, frontletId));
        return handlers;
    }

    addHandlerIfMatching(handlers, entry, frontletId) {
        if (!entry || !entry.active || !entry.element.parentNode) {
            return;
        }
        var handler = app.tagHandlers.getHandler(entry.element);
        if (handler && handler.frontletInstance && handler.frontletInstance.frontlet.id === frontletId) {
            if (handlers.indexOf(handler) < 0) {
                handlers.push(handler);
            }
        }
    }

    /**
     * @param {Element} element 
     * @param {String} id
     */
    addContainer(element, id) {
        if (!id) {
            this.anonymousContainers.push({ element: element, active: true });
            return;
        }
        this.containers[id] = { element: element, active: true };
    }

    deactivateAll() {
        for (var id of Object.keys(this.containers)) {
            this.containers[id].active = false;
        }
        this.anonymousContainers.forEach(entry => entry.active = false);
    }

    reset() {
        this.containers = {};
        this.anonymousContainers = [];
    }
}
