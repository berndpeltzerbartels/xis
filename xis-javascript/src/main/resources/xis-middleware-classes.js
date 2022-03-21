
XISPages.prototype.addPage = function(page) {
    this.pages[page.path] = path;
}

XISPages.prototype.getPageByPath = function(path) {
    return this.pages[path];
}

function XISLifecycleService() {
    this.remoteService = new XISRemoteService();
}

XISLifecycleService.prototype.onInitWidget = function(widget) {
  // TODO if (arrayContains(this.widgetsOnInit)) {
        this.remoteService.onInitWidget(widget, function(data) {
            widget.updateData(data);
            widget.update();
        });
   // }
}

XISLifecycleService.prototype.onDisplayWidget = function(widget) {
    // TODO if (arrayContains(this.widgetsOnDisplay)) {
        this.remoteService.onDisplayWidget(widget, function(data) {
            widget.updateData(data);
            widget.update();
        });
   
   // }
}

XISLifecycleService.prototype.onHideWidget = function(widget) {
     // TODOif (arrayContains(this.widgetsOnClose)) {
        this.remoteService.onHideWidget(widget);
    //}
}

function XISRemoteService () {
    this.httpClient = new XISHttpClient();
}

XISRemoteService.prototype.onInitWidget = function(widget, callback) {
    var service = this;
    setTimeout(function() {
        service.send(service.createOnInitWidgetMessage(widget), callback);
    }, 0);
}


XISRemoteService.prototype.onDisplayWidget = function(widget, callback) {
    var service = this;
    setTimeout(function() {
        service.send(service.createOnDisplayWidgetMessage(widget), callback);
    }, 0);
}

XISRemoteService.prototype.onHideWidget= function(widget) {
    var service = this;
    setTimeout(function() {
        service.send(service.createOnCloseWidgetMessage(widget));
    }, 0);
    
}

XISRemoteService.prototype.send = function(message, callback) {
    this.httpClient.post('/__xis.con', message, callback);
}

XISRemoteService.prototype.createOnInitWidgetMessage = function(widget) {
    var message =  {
        type: 'widget-lifecycle',
        phase: 'init',
        elementId: widget.name
    };
    message.hash = this.hashCode(message);
    return message;
}

XISRemoteService.prototype.createOnDisplayWidgetMessage = function(widget) {
    var message = {
        type: 'widget-lifecycle',
        phase: 'display',
        elementId: widget.name,
        state: widget.getState()
    };
    message.hash = this.hashCode(message);
    return message;
}

XISRemoteService.prototype.createOnCloseWidgetMessage = function(widget) {
    var message = {
        type: 'widget-lifecycle',
        phase: 'close',
        elementId: widget.name,
        state: widget.getState()
    };
    message.hash = this.hashCode(message);
    return message;
}

XISRemoteService.prototype.hashCode = function(message) {
    return '';
}

function XISHttpClient() {}

XISHttpClient.prototype.get = function (uri, callback) {
    this.execute('GET', uri, null, callback);
}

XISHttpClient.prototype.post = function (uri, payload, callback) {
    this.execute('POST', uri, payload, callback);
}

XISHttpClient.prototype.post = function (uri, payload, callback) {
    this.execute('PUT', uri, payload, callback);
}

XISHttpClient.prototype.execute = function (method, uri, payload, callback) {
    var http = this.http();
    http.open(method, uri, true);
    http.onreadystatechange = function () {
        if (http.readyState == 4) {
            callback(JSON.parse(http.response));
        }
    };
    if (payload) {
        http.send(JSON.stringify(payload));
    } else {
        http.send(null);
    }
}

XISHttpClient.prototype.http = function() {
    if (typeof window.XMLHttpRequest === 'function') {
        return new XMLHttpRequest();
    }
    return new ActiveXObject("Microsoft.XMLHTTP");
}
