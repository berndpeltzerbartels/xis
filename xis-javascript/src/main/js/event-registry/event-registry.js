// enum of event types
const EventType = {
    APP_INITIALIZED: 'app_initialized',
    PAGE_LOADED: 'page_loaded',
    WIDGET_LOADED: 'widget_loaded',
    FORM_LOADED: 'form_loaded',
    WIDGET_ACTION: 'widget_action',
    PAGE_ACTION: 'page_action',
    FORM_ACTION: 'form_action',
    SUBMIT: 'submit',
    REQUEST_COMPLETED: 'request_completed'
};

window['EventType'] = EventType;
window['eventListenerRegistry'] = {
    listeners: {},

  addEventListener : function(type, listener) {
    if (!this.listeners[type]) this.listeners[type] = [];
    this.listeners[type].push(listener);
  }
};
