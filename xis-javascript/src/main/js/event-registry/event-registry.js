// enum of event types
const EventType = {
    APP_INSTANCE_CREATED: 'app_instance_created',
    APP_INITIALIZED: 'app_initialized',
    PAGE_LOADED: 'page_loaded',
    FRONTLET_LOADED: 'frontlet_loaded',
    FORM_LOADED: 'form_loaded',
    FRONTLET_ACTION: 'frontlet_action',
    PAGE_ACTION: 'page_action',
    FORM_ACTION: 'form_action',
    SUBMIT: 'submit',
    BUFFER_COMMITTED: 'buffer_commited',
    REACTIVE_DATA_CHANGED: 'reactive_data_changed'
};

window['EventType'] = EventType;
window['eventListenerRegistry'] = {
    listeners: {},

  addEventListener : function(type, listener) {
    if (!this.listeners[type]) this.listeners[type] = [];
    this.listeners[type].push(listener);
  }
};
