
class TestEventConnector {

    constructor(clientId) {
        this.clientId = clientId;
    }

    setPendingEventTtlMs(_) {
    }

    ensureConnected() {
        return Promise.resolve();
    }

    simulatePushEvent(updateEventKey) {
        return app.pageController.handleUpdateEvents([updateEventKey])
            .then(pageUpdated => {
                if (!pageUpdated) {
                    return app.frontletContainers.handleUpdateEvents([updateEventKey]);
                }
                return pageUpdated;
            })
            .catch(e => reportError('TestEventConnector failed for update-event key=' + updateEventKey, e));
    }
}
