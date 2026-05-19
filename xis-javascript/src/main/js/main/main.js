 var app;

/** @noinline */
function main() {
    app = new Application();
    app.eventPublisher.listeners = eventListenerRegistry.listeners;
    installPublicApi();
    app.start();

    // backward-button
    window.addEventListener('popstate', event => app.history.onPopState(event));

}

function installPublicApi() {
    installXisPublicApi({
        submitForm(formId, action) {
            return app.submitForm(formId, action);
        },

        openPage(url) {
            return app.pageController.displayPageForUrl(url);
        },

        isEventStreamConnected() {
            return !!(app.eventConnector && app.eventConnector.isConnected());
        },

        closeEventStreams() {
            if (app.eventConnector) {
                app.eventConnector.close();
            }
        }
    });
}

function installXisPublicApi(methods) {
    const currentApi = window['XIS'] || {};
    const api = {};
    for (const key in currentApi) {
        api[key] = currentApi[key];
    }
    for (const key in methods) {
        api[key] = methods[key];
    }
    if (typeof XIS !== 'undefined') {
        XIS = api;
    }
    window['XIS'] = Object.freeze ? Object.freeze(api) : api;
}


window['main'] = main;
