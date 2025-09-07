var app;

/** @noinline */
function main() {
debugger;
    app = new Application();
    app.eventPublisher.listeners = eventListenerRegistry.listeners;
    window['app'] = app;
    app.start();

    // backward-button
    window.addEventListener('popstate', event => app.history.onPopState(event));

}


window['main'] = main;
