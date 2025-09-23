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
    CLIENT_STATE_CHANGED: 'client_state_changed',
    LOCAL_STORAGE_CHANGED: 'local_storage_changed'
};

window['EventType'] = EventType;
window['eventListenerRegistry'] = {
    listeners: {},

  addEventListener : function(type, listener) {
    if (!this.listeners[type]) this.listeners[type] = [];
    this.listeners[type].push(listener);
  }
};
