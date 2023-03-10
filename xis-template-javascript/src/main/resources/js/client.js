/**
 * A function called with an element as parameter without any return
 * @callback Callback
 * @param {any} value
 */

/**
 * @property {HttpClient} httpClient
 * @property {Config} config
 */
class Client {

    /**
     * 
     * @param {HttpClient} httpClient 
     */
    constructor(httpClient) {
        this.httpClient = httpClient;
        this.config = undefined;
    }

    /**
     * @public
     * @param {Callback} callback 
     */
    loadConfig(callback) {
        var client = this;
        this.httpClient.get('/xis/config', config => {
            client.config = config;
            callback(config)
        });
    }

    /**
     * @public
     * @param {string} pageId
     * @param {Callback} callback 
     */
    loadPageHead(pageId, callback) {
        this.httpClient.get('/xis/page/' + pageId + '/head', callback)
    }

    /**
     * @public
     * @param {string} pageId
     * @param {Callback} callback 
     */
    loadPageBody(pageId, callback) {
        this.httpClient.get('/xis/page/' + pageId + '/body', callback)
    }



    /**
     * @public
     * @param {string} pageId
     * @param {Data} data
     * @param {Callback} callback 
     */
    loadPageData(pageId, data, dataCalllback) {
        this.postForPage(pageId, data, dataCalllback);
    }

    /**
     * @public
     * @param {string} widgetId
     * @param {Data} data
     * @param {Callback} callback 
     */
    loadWidgetData(widgetId, data, dataCalllback) {
        this.postForWidget(widgetId, data, dataCalllback);
    }

    /**
     * @public
     * @param {string} widgetId
     * @param {Data} data
     * @param {Callback} callback 
     */
    onShowWidget(widgetId, data, dataCalllback) {
        this.postForWidget(widgetId, data, dataCalllback, 'show');
    }


    /**
     * @public
     * @param {string} widgetId
     * @param {Data} data
     * @param {Callback} callback 
     */
    onHideWidget(widgetId, data, dataCalllback) {
        this.postForWidget(widgetId, data, dataCalllback, 'hide');
    }
    /**
     * @public
     * @param {string} widgetId
     * @param {Data} data
     * @param {Callback} callback 
     */
    onDestroyWidget(widgetId, data, dataCalllback) {
        this.postForWidget(widgetId, data, dataCalllback, 'destroy');
    }

    /**
     * @private
     * @param {string} widgetId 
     * @param {Data} data 
     * @param {Callback} dataCalllback
     */
    postForWidget(widgetId, data, dataCalllback) {
        var request = this.createWidgetRequest(widgetId, data);
        this.httpClient.post('/xis/widget/' + widgetId, request, dataCalllback);
    }

    /**
    * @private
    * @param {string} pageId 
    * @param {Data} data 
    * @param {Callback} dataCalllback
    */
    postForPage(pageId, data, dataCalllback) {
        var request = this.createPageModelRequest(pageId, data);
        this.httpClient.post('/xis/page/' + pageId, request, dataCalllback);
    }


    /**
     * @private
     * @param {any} data 
     */
    createPageModelRequest(pageId, data) {
        return new ModelRequest(pageId, 'page', this.createMethodInvocations(methodConfigs, data));
    }

    /**
    * @private
    * @param {any} data 
    * @param {string} phasis 
    */
    createWidgetRequest(widgetId, data) {
        return new ModelRequest(widgetId, 'widget', this.createMethodInvocations(config.getModelFactories(), data));
    }


    /**
     * @private
     * @param {array<MethodConfig>} methodConfigs
     * @param {Data} data
     */
    createMethodInvocations(methodConfigs, data) {
        return methodConfigs.map(config => this.createMethodInvocation(config, data));
    }

    /**
     * @private
     * @param {MethodConfig} methodConfig 
     * @param {Data} data
     */
    createMethodInvocation(methodConfig, data) {
        var parameters = {};
        methodConfig.parameters.forEach(param => parameters[param.name] = data.getValue(param.path));
        return new MethodInvocation(methodConfig.name, parameters);
    }


}


class HttpClient {

    /**
     * @param {Function} errorHandler 
     */
    constructor(errorHandler) {
        this.className = 'HttpClient';
        this.errorHandler = errorHandler;
    }

    post(uri, payload, handler) {
        var payloadJson = JSON.stringify(payload);
        headers['Content-length'] = payloadJson.length;
        headers['Content-type'] = 'application/json';
        this.doRequest(uri, headers, 'POST', payloadJson, handler);
    }

    get(uri, handler) {
        this.doRequest(uri, {}, 'POST', undefined, handler);
    }

    /**
     * @private
     * @param {string} uri 
     * @param {any} headers
     * @param {string} method 
     * @param {any} payload 
     * @param {Function} handler 
     */
    doRequest(uri, headers, method, payload, handler) {
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.open(method, uri, true); // true for asynchronous
        for (var name of Object.keys(headers)) {
            xmlHttp.setRequestHeader(name, headers[name]);
        }
        xmlHttp.onreadystatechange = function () {
            // TODO Handle errors and "304 NOT MODIFIED"
            // TODO Add headers to allow 304
            // Readystaet == 4 for 304 ?
            if (xmlHttp.readyState == 4 && xmlHttp.status == 200) { // TODO In Java 204 if there is no server-method
                handler(JSON.parse(xmlHttp.responseText));
            }
            // TODO use errorhandler
        }
        if (payload) {
            xmlHttp.send(payload);
        }
        else {
            xmlHttp.send();
        }
    }

}

/**
 * @property {string} id
 * @property {type} type  'page' or 'widget'
 * @property {array<MethodInvocation} methodInvocations
 */
class ModelRequest {

    constructor(id, type, methodInvocations) {
        this.id = id;
        this.type = type;
        this.methodInvocations = methodInvocations;
    }
}


class MethodInvocation {

    constructor(methodName, parameters) {
        this.methodName = methodName;
        this.parameters = parameters;
    }
}

