// enum of event types
const EventType = {
    APP_INITIALIZED: 'app_initialized',
    PAGE_LOADED: 'page_loaded',
    WIDGET_LOADED: 'widget_loaded',
    FORM_LOADED: 'form_loaded',
    WIDGET_ACTION: 'widget_action',
    PAGE_ACTION: 'page_action',
    FORM_ACTION: 'form_action',
    SUBMIT: 'submit'
};

window['EventType'] = EventType;
window['eventListenerRegistry'] = {
    listeners: {},

  addEventListener : function(type, listener) {
    if (!this.listeners[type]) this.listeners[type] = [];
    this.listeners[type].push(listener);
  }
};

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
