/**
 * @typedef Client
 * @property {HttpClient} httpClient
 * @property {ClientConfig} config
 * @property {string} clientId
 * @property {string} userId
 */
class Client {

    /**
     * @param {HttpClient} httpClient
     */
    constructor(httpClient) {
        this.httpClient = httpClient;
        this.config = undefined;
        this.clientId = '';
        this.userId = '';
    }

    /**
     * @public
     * @return {Promise<ClientConfig>}
     */
    loadConfig() {
        var _this = this;
        return this.httpClient.get('/xis/config', {})
            .then(content => _this.deserializeConfig(content))
            .then(config => { _this.config = config; return config; });
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageHead(pageId) {
        return this.httpClient.get('/xis/page/head', { uri: pageId });
    }


    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageBody(pageId) {
        return this.httpClient.get('/xis/page/body', { uri: pageId });
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<any>}
     */
    loadPageBodyAttributes(pageId) {
        return this.httpClient.get('/xis/page/body-attributes', { uri: pageId }).then(content => JSON.parse(content));
    }

    /**
    * @public
    * @param {string} pageId
    * @return {Promise<string>}
    */
    loadWidget(widgetId) {
        return this.httpClient.get('/xis/widget/html/' + widgetId, {});
    }

    /**
     * @public
     * @param {ResolvedURL} resolvedURL 
     * @param {Data} data
     * @returns {Promise<any>}
     */
    loadPageData(resolvedURL, data) {
        var _this = this;
        var request = this.createPageRequest(resolvedURL, data, null, null);
        return this.httpClient.post('/xis/page/model', request, {})
            .then(content => _this.deserializeResponse(content));
    }

    /**
    * @public
    * @param {string} widgetId
    * @param {WidgetInstance} widgetInstance
    * @returns {Promise<ServerReponse>}
    */
    loadWidgetData(widgetInstance, widgetState) {
        var _this = this;
        var request = this.createWidgetRequest(widgetInstance, widgetState, null, null);
        return this.httpClient.post('/xis/widget/model', request)
            .then(content => _this.deserializeResponse(content));
    }


    /**
     * @public
     * @param {WidgetInstance} widgetInstance
     * @param {string} action
     * @returns {Promise<ServerReponse>}
     */
    widgetAction(widgetInstance, widgetState, action) {
        var _this = this;
        var request = this.createWidgetRequest(widgetInstance, widgetState, action, null);
        return this.httpClient.post('/xis/widget/action', request, {})
            .then(content => _this.deserializeResponse(content));
    }

    /**
     * @public
     * @param {WidgetInstance} widgetInstance
     * @param {WidgetState} widgetState
     * @param {string} action
     * @param {Data} formData    
     * @returns {Promise<ServerReponse>}
     */
    widgetFormAction(widgetInstance, widgetState, action, formData) {
        var _this = this;
        var request = this.createWidgetRequest(widgetInstance, widgetState, action, formData);
        return this.httpClient.post('/xis/widget/action', request, {})
            .then(content => _this.deserializeResponse(content));
    }

    /**
     * @public
     * @param {ResolvedURL} resolvedURL 
     * @param {Data} data
     * @param {string} action
     * @returns {Promise<ServerReponse>}
     */
    pageAction(resolvedURL, data, action) {
        var _this = this;
        var request = this.createPageRequest(resolvedURL, data, action, null);
        return this.httpClient.post('/xis/page/action', request, {})
            .then(content => _this.deserializeResponse(content));
    }

    /**
   * @public
   * @param {ResolvedURL} resolvedURL 
   * @param {Data} pageData
   * @param {string} action
   * @param {Data} formData 
   * @returns {Promise<ServerReponse>}
   */
    pageFormAction(resolvedURL, pageData, action, formData) {
        var _this = this;
        var request = this.createPageRequest(resolvedURL, pageData, action, formData);
        return this.httpClient.post('/xis/page/action', request, {})
            .then(content => _this.deserializeResponse(content));
    }

    /**
     * @private
     * @param {ResolvedURL} resolvedURL 
     * @param {Data} data
     * @param {string} action (nullable)
     * @param {Data} formData (nullable)
     * @returns {ClientRequest}
     */
    createPageRequest(resolvedURL, data, action, formData) {
        var normalizedPath = resolvedURL.normalizedPath;
        var request = new ClientRequest();
        request.clientId = this.clientId;
        request.userId = this.userId;
        request.pageId = normalizedPath;
        request.action = action;
        request.data = action ? this.pageActionParameters(action, data, normalizedPath) : this.pageModelParameters(data, normalizedPath);
        request.formData = formData ? formData.values : {};
        request.urlParameters = resolvedURL.urlParameters;
        request.pathVariables = resolvedURL.pathVariablesAsMap();
        return request;
    }

    /**
    * @private
    * @param {string} widgetId 
    * @param {WidgetInstance} widgetInstance
    * @param {WidgetState} widgetState
    * @param {string} action (nullable)
    * @param {Data} formData (nullable)
    * @returns {ClientRequest}
    */
    createWidgetRequest(widgetInstance, widgetState, action, formData) {
        var request = new ClientRequest();
        request.clientId = this.clientId;
        request.userId = this.userId;
        request.widgetId = widgetInstance.widget.id;
        request.action = action;
        request.data = action ? this.widgetActionParameters(widgetInstance, widgetState, action) : this.widgetModelParameters(widgetInstance, widgetState);
        request.formData = formData ? formData.values : {};
        request.urlParameters = widgetState.resolvedURL.urlParameters;
        request.pathVariables = widgetState.resolvedURL.pathVariablesAsMap();
        request.widgetParameters = widgetState.widgetParameters;
        return request;
    }


    /**
     * @private
     * @param {string} content 
     * @returns {ClientConfig}
     */
    deserializeConfig(content) {
        var obj = JSON.parse(content);
        var config = new ClientConfig();
        config.welcomePageId = obj.welcomePageId;
        config.pageIds = obj.pageIds ? obj.pageIds : [];
        config.widgetIds = obj.widgetIds ? obj.widgetIds : [];
        config.pageAttributes = {};
        if (obj.pageAttributes) {
            for (var key of Object.keys(obj.pageAttributes)) {
                config.pageAttributes[key] = new PageAttributes(obj.pageAttributes[key]);
            }
        }
        config.widgetAttributes = {};
        if (obj.widgetAttributes) {
            for (var key of Object.keys(obj.widgetAttributes)) {
                config.widgetAttributes[key] = new WidgetAttributes(obj.widgetAttributes[key]);
            }
        }
        return config;
    }

    /**
     * Selects parameters being used in model data method's signature.
     * 
     * @private
     * @param {string} content 
     * @returns {ServerReponse}
     */
    deserializeResponse(content) {
        var obj = JSON.parse(content);
        var data = obj.data ? new Data(JSON.parse(obj.data)) : new Data({});
        var response = new ServerResponse();
        response.data = data;
        response.nextPageURL = obj.nextPageURL;
        response.nextWidgetId = obj.nextWidgetId;
        data.setValue(['validation'], obj.validationResult);
        return response;

    }

    /**
    * @private
    * @param {Data} data
    * @param {string} normalizedPath
    * @returns {string: string}
    */
    pageModelParameters(data, normalizedPath) {
        var result = {};
        var attributes = this.config.pageAttributes[normalizedPath];
        var keys = attributes.modelParameterNames;
        if (keys) {
            for (var key of keys) {
                result[key] = data.getValue([key]);
            }
        }
        return result;
    }


    /**
     * Selects parameters being used in model data method's signature.
     * 
    * @private
    * @param {WidgetInstance} widgetInstance
    * @param {WidgetState} widgetState
    * @returns {string: string}
    */
    widgetModelParameters(widgetInstance, widgetState) {
        var result = {};
        var widgetId = widgetInstance.widget.id;
        var data = widgetState.data;
        var attributes = this.config.widgetAttributes[widgetId];
        var keys = attributes.modelParameterNames;
        if (keys) {
            for (var key of keys) {
                result[key] = data.getValue([key]);
            }
        }
        return result;
    }

    /**
     * Selects parameters being used in action method's signature.
     * 
    * @private
    * @param {string} action
    * @param {Data} data
    * @param {string} normalizedPath 
    * @returns {string: string}
    */
    pageActionParameters(action, data, normalizedPath) {
        var result = {};
        var attributes = this.config.pageAttributes[normalizedPath];
        var keys = attributes.actionParameterNames[action];
        if (keys) {
            for (var key of keys) {
                result[key] = data.getValue([key]);
            }
        }
        return result;
    }

    /**
    * Selects parameters being used in action method's signature. 
    * @private
    * @param {WidgetInstance} widgetInstance
    * @param {WidgetState} widgetState
    * @param {string} action 
    * @returns {string: string}
   */
    widgetActionParameters(widgetInstance, widgetState, action) {
        var result = {};
        var widgetId = widgetInstance.widget.id;
        var data = widgetState.data;
        var attributes = this.config.widgetAttributes[widgetId];
        var keys = attributes.actionParameterNames[action];
        if (keys) {
            for (var key of keys) {
                result[key] = data.getValue([key]);
            }
        }
        return result;
    }

}
