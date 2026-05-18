
class Client {


    /** * @constructor
     * Initializes a new instance of the Client class.
     * This class serves as a base for HTTP clients that interact with the server.
     * It provides methods to load configuration, page data, frontlet data, and handle server responses
     */
    constructor(clientId) {
        this.config = undefined;
        this.clientId = clientId;
        this.zoneId = timeZone();
    }

    /**
  * @public
  * @return {Promise<ClientConfig>}
  */
    loadConfig() {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageHead(pageId) {
        throw new Error('Not implemented');
    }


    /**
     * @public
     * @param {string} pageId
     * @return {Promise<string>}
     */
    loadPageBody(pageId) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @param {string} pageId
     * @return {Promise<any>}
     */
    loadPageBodyAttributes(pageId) {
        throw new Error('Not implemented');
    }

    /**
    * @public
    * @param {string} pageId
    * @return {Promise<string>}
    */
    loadFrontlet(frontletId) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @param {ResolvedURL} resolvedURL 
     * @returns {Promise<any>}
     */
    loadPageData(resolvedURL) {
        throw new Error('Not implemented');
    }

    /**
    * @public
    * @param {FrontletInstance} frontletInstance
    * @returns {Promise<ServerReponse>}
    */
    loadFrontletData(frontletInstance, frontletState) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @param {ResolvedURL} resolvedURL
     * @param {String} frontletId
     * @param {String} formBindingKey
     * @param {any} formBindingParameters
     */
    loadFormData(resolvedURL, frontletId, formBindingKey, formBindingParameters) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @param {FrontletInstance} frontletInstance
     * @param {string} action
     * @param {string} actionParameters
     * @returns {Promise<ServerReponse>}
     */
    frontletLinkAction(frontletInstance, frontletState, action, actionParameters) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @param {ResolvedURL} resolvedURL 
     * @param {string} action
     * @param {any} actionParameters
     * @returns {Promise<ServerReponse>}
     */
    pageLinkAction(resolvedURL, action, actionParameters) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @param {ResolvedURL} resolvedURL 
     * @param {String} frontletId
     * @param {string:string} formData
     * @param {string} action
     * @param {any} actionParameters
     * @param {string} binding
     * @returns {Promise<ServerReponse>}
     */
    formAction(resolvedURL, frontletId, formData, action, formBindigKey, formBindingParameters, uploads) {
        throw new Error('Not implemented');
    }

    /**
     * @public
     * @param {string} renewToken
     * @returns {Promise<TokenResponse>}
     */
    sendRenewTokenRequest(renewToken) {
        throw new Error('Not implemented');
    }



     async handleResponse(response) {
        if (this.serverError(response)) {
            this.handleServerError(response);
            return Promise.reject();
        }
        if (this.isAjaxRedirect(response)) {
            // follow redirect in browser
            return Promise.reject({type: 'redirect'});
        }
        if (this.authorizationRequired(response)) {
            this.forwardToLoginPage(response);
            return Promise.reject({type: 'redirect'});
        }
        if (this.isBrowserRedirect(response)) {
            this.doBrowserRedirect(response);
            return Promise.reject({type: 'redirect'});
        }
        var responseObject = this.deserializeResponse(response);
        if (responseObject.redirectUrl) {
            this.forward(responseObject.redirectUrl);
            return Promise.reject({type: 'redirect'});
        }
        const globalMessages = this.globalValidatorMessages(responseObject);
        if (globalMessages.length > 0) {
            app.messageHandler.addValidationErrors(globalMessages);
        }
        return Promise.resolve(responseObject);
     }

     forwardToLoginPage(response) {
         var redirectUri = response.getResponseHeader('Location');
         this.forward(redirectUri);
     }

     forward(redirectUri) {
         if (typeof app !== 'undefined' && typeof app.prepareForBrowserNavigation === 'function') {
             app.prepareForBrowserNavigation();
         }
         window.location.href = redirectUri;
     }

     authorizationRequired(response) {
         return response.status === 401;
     }

     serverError(response) {
         return response.status >= 500 && response.status < 600;
     }

     isAjaxRedirect(response) {
         return response.status == 302 || response.status == 303 || response.status == 307 || response.status == 308;
     }

     doBrowserRedirect(response) {
         var redirectUri = response.getResponseHeader('Location');
         this.forward(redirectUri);
     }

     isBrowserRedirect(response) {
         return !this.isAjaxRedirect(response) && response.getResponseHeader('Location');
     }

     handleServerError(response) {
         console.error('Server error occurred:', response); // do not use reportError(...), here
         return app.messageHandler.reportServerError(this.readServerErrorMessage(response));
     }

     readServerErrorMessage(response) {
         if (!response || !response.responseText) {
             return 'Internal server error';
         }
         try {
             var parsed = JSON.parse(response.responseText);
             return parsed.message || response.responseText;
         } catch (e) {
             return response.responseText;
         }
     }

     globalValidatorMessages(response) {
         const globalMessages = response.validatorMessages ? response.validatorMessages.globalMessages : [];
         if (Array.isArray(globalMessages)) {
             return globalMessages.filter(s => s && s.trim().length > 0);
         }
         return [];
     }


    /**
     * @protected
     * @param {ResolvedURL} resolvedURL 
     * @param {string} action (nullable)
     * @param {Data} formData (nullable)
     * @param {any} actionParameters (nullable)
     * @returns {ClientRequest}
     */
    createPageRequest(resolvedURL, formData, action, actionParameters) {
        var normalizedPath = resolvedURL.normalizedPath;
        var request = new ClientRequest();
        request.clientId = this.clientId;
        request.pageId = normalizedPath;
        request.pageUrl = resolvedURL.url;
        request.action = action;
        request.formData = formData ? formData.values : {};
        request.urlParameters = resolvedURL.urlParameters;
        request.pathVariables = resolvedURL.pathVariablesAsMap();
        request.actionParameters = this.serializeParameterMap(actionParameters);
        request.zoneId = this.zoneId;
        request.sessionStorageData = this.sessionStorageDataPage(normalizedPath);
        request.localStorageData = this.localStorageDataPage(normalizedPath);
        request.clientStorageData = this.clientStorageDataPage(normalizedPath);
        request.globalVariableData = this.globalVariableDataPage(normalizedPath);
        request.localDatabaseData = {};
        request.type = 'page';
        return request;
    }

    /**
     * @protected
     * @param {ResolvedURL} resolvedURL
     * @param {String} frontletId
     * @param {string:string} formData
     * @param {String} action
     * @param {any} actionParameters
     */
    createFormRequest(resolvedURL, frontletId, formData, action, formBindingKey, frontletParameters) {
        var mappedFormData = {};
        if (formBindingKey) {
            mappedFormData[formBindingKey] = formData;
        }
        var normalizedPath = resolvedURL.normalizedPath;
        var request = new ClientRequest();
        request.clientId = this.clientId;
        request.frontletId = frontletId;
        request.pageId = normalizedPath;
        request.pageUrl = resolvedURL.url;
        request.formBinding = formBindingKey;
        request.action = action;
        request.formData = mappedFormData;
        request.urlParameters = resolvedURL.urlParameters;
        request.pathVariables = resolvedURL.pathVariablesAsMap();
        request.frontletParameters = this.serializeParameterMap(frontletParameters);
        request.zoneId = this.zoneId;
        request.type = request.frontletId ? 'frontlet' : 'page';
        if (frontletId) { // TODO write a test
            request.sessionStorageData = this.sessionStorageDataFrontlet(frontletId);
            request.localStorageData = this.localStorageDataFrontlet(frontletId);
            request.clientStorageData = this.clientStorageDataFrontlet(frontletId);
            request.globalVariableData = this.globalVariableDataFrontlet(frontletId);
        }
        if (normalizedPath) {// TODO write a test
            request.sessionStorageData = this.sessionStorageDataPage(normalizedPath);
            request.localStorageData = this.localStorageDataPage(normalizedPath);
            request.clientStorageData = this.clientStorageDataPage(normalizedPath);
            request.globalVariableData = this.globalVariableDataPage(normalizedPath);
        }
        return request;
    }

    /**
    * @protected
    * @param {string} frontletId
    * @param {FrontletInstance} frontletInstance
    * @param {FrontletState} frontletState
    * @param {string} action (nullable)
    * @param {Data} formData (nullable)
    * @param {string} actionParameters (nullable)
    * @returns {ClientRequest}
    */
    createFrontletRequest(frontletInstance, frontletState, action, formData, actionParameters) {
        var request = new ClientRequest();
        var normalizedPath = frontletState.resolvedURL.normalizedPath;
        request.clientId = this.clientId;
        request.pageId = normalizedPath;
        request.pageUrl = frontletState.resolvedURL.url;
        request.frontletId = frontletInstance.frontlet.id;
        request.action = action;
        request.formData = formData ? formData.values : {};
        request.urlParameters = frontletState.resolvedURL.urlParameters;
        request.pathVariables = frontletState.resolvedURL.pathVariablesAsMap();
        request.frontletParameters = this.serializeParameterMap(frontletState.frontletParameters);
        request.actionParameters = this.serializeParameterMap(actionParameters);
        request.zoneId = this.zoneId;         // TODO locale ?
        request.sessionStorageData = this.sessionStorageDataFrontlet(frontletInstance.frontlet.id);
        request.localStorageData = this.localStorageDataFrontlet(frontletInstance.frontlet.id);
        request.clientStorageData = this.clientStorageDataFrontlet(frontletInstance.frontlet.id);
        request.globalVariableData = this.globalVariableDataFrontlet(frontletInstance.frontlet.id);
        request.localDatabaseData = {};
        request.type = 'frontlet';
        return request;
    }

    serializeParameterMap(parameters) {
        const serialized = {};
        if (!parameters) {
            return serialized;
        }
        for (var key of Object.keys(parameters)) {
            serialized[key] = JSON.stringify(parameters[key]);
        }
        return serialized;
    }

    localStorageDataPage(pageId) {
        return this.localStorageData(this.config.pageAttributes[pageId]);
    }

    sessionStorageDataPage(pageId) {
        return this.sessionStorageData(this.config.pageAttributes[pageId]);
    }

    sessionStorageDataFrontlet(frontletId) {
        return this.sessionStorageData(this.config.frontletAttributes[frontletId]);
    }

    localStorageDataFrontlet(frontletId) {
        return this.localStorageData(this.config.frontletAttributes[frontletId]);
    }

    clientStorageDataPage(pageId) {
        return this.clientStorageData(this.config.pageAttributes[pageId]);
    }

    clientStorageDataFrontlet(frontletId) {
        return this.clientStorageData(this.config.frontletAttributes[frontletId]);
    }

    globalVariableDataPage(pageId) {
        return this.globalVariableData(this.config.pageAttributes[pageId]);
    }

    globalVariableDataFrontlet(frontletId) {
        return this.globalVariableData(this.config.frontletAttributes[frontletId]);
    }


    sessionStorageData(attributes) {
        var data = {};
        for (var key of attributes.sessionStorageKeys) {
            data[key] = app.sessionStorage.getValue(key);
        }
        return data;
    }


    localStorageData(attributes) {
        var data = {};
        for (var key of attributes.localStorageKeys) {
            data[key] = app.localStorage.getValue(key);
        }
        return data;
    }

    clientStorageData(attributes) {
        var data = {};
        for (var key of attributes.clientStorageKeys) {
            data[key] = app.clientStorage.getValue(key);
        }
        return data;
    }

    globalVariableData(attributes) {
        var data = {};
        for (var key of attributes.globalVariableKeys) {
            data[key] = app.globals.getValue(key);
        }
        return data;
    }



    /**
     * @protected
     * @param {string} content 
     * @returns {ClientConfig}
     */
    deserializeConfig(content) {
        var obj = JSON.parse(content);
        var config = new ClientConfig();
        config.welcomePageId = obj.welcomePageId;
        config.pageIds = obj.pageIds ? obj.pageIds : [];
        config.frontletIds = obj.frontletIds ? obj.frontletIds : (obj.frontletIds ? obj.frontletIds : []);
        config.includeIds = obj.includeIds ? obj.includeIds : [];
        config.pendingEventTtlSeconds = obj.pendingEventTtlSeconds || 0;
        config.pageAttributes = {};
        if (obj.pageAttributes) {
            for (var key of Object.keys(obj.pageAttributes)) {
                config.pageAttributes[key] = new PageAttributes(obj.pageAttributes[key]);
            }
        }
        config.frontletAttributes = {};
        var frontletAttributes = obj.frontletAttributes ? obj.frontletAttributes : obj.frontletAttributes;
        if (frontletAttributes) {
            for (var key of Object.keys(frontletAttributes)) {
                config.frontletAttributes[key] = new FrontletAttributes(frontletAttributes[key]);
            }
        }
        return config;
    }

    /**
     * Selects parameters being used in model data method's signature.
     * 
     * @protected
     * @param {Response} content 
     * @param {number} httpStatus
     * @returns {ServerResponse}
     */
    deserializeResponse(response) {
        var obj = response.body ? response.body : JSON.parse(response.responseText); // for ws, we have a body
        var data = obj.data ? new Data(obj.data) : new Data({});
        var formData = obj.formData ? new Data(obj.formData) : new Data({});
        var serverResponse = new ServerResponse();
        serverResponse.data = data;
        serverResponse.formData = formData;
        serverResponse.nextURL = obj.nextURL;
        serverResponse.nextFrontletId = obj.nextFrontletId ? obj.nextFrontletId : obj.nextFrontletId;
        serverResponse.nextModalId = obj.nextModalId;
        serverResponse.closeModal = obj.closeModal === true;
        serverResponse.reloadModalParent = obj.reloadModalParent === true;
        serverResponse.status = response.status;
        serverResponse.validatorMessages = new ValidatorMessages(obj.validatorMessages);
        serverResponse.reloadPage = obj.reloadPage;
        serverResponse.reloadFrontlets = obj.reloadFrontlets ? obj.reloadFrontlets : obj.reloadFrontlets;
        serverResponse.localDatabaseData = obj.localDatabaseData || {};
        serverResponse.localStorageData = obj.localStorageData || {};
        serverResponse.clientStorageData = obj.clientStorageData || {};
        serverResponse.globalVariableData = obj.globalVariableData || {};
        serverResponse.sessionStorageData = obj.sessionStorageData || {};
        serverResponse.authenticated = obj.authenticated === true;
        serverResponse.userRoles = Array.isArray(obj.userRoles) ? obj.userRoles : [];
        serverResponse.frontletParameters = obj.frontletParameters ? obj.frontletParameters : (obj.frontletParameters || {});
        serverResponse.frontletContainerId = obj.frontletContainerId ? obj.frontletContainerId : obj.frontletContainerId;
        serverResponse.redirectUrl = obj.redirectUrl;
        serverResponse.idVariables = obj.idVariables || {};
        serverResponse.actionProcessing = obj.actionProcessing || 'NONE';
        serverResponse.updateEventKeys = obj.updateEventKeys || [];
        serverResponse.annotatedTitle = obj.annotatedTitle; // difference between null and '' is important
        serverResponse.annotatedAddress = obj.annotatedAddress;
        serverResponse.defaultFrontlets = obj.defaultFrontlets ? obj.defaultFrontlets : (obj.defaultFrontlets || []);
        const validatorMessages = obj.validatorMessages || {};
        data.setValue(['sessionStorage'], app.sessionStorage);
        data.setValue(['localStorage'], app.localStorage);
        data.setValue(['clientStorage'], serverResponse.clientStorageData);
        data.setValue(['global'], serverResponse.globalVariableData);
        data.setValue(['messages'], app.messages || {});
        data.setValue(['validation'], validatorMessages);
        data.setValue(['validation','globalMessages'], Array.isArray(validatorMessages.globalMessages) ? validatorMessages.globalMessages : []);
        data.setValue(['_xis', 'authenticated'], serverResponse.authenticated);
        data.setValue(['_xis', 'roles'], serverResponse.userRoles);
        data.setValue(['origin'], window.location.origin);
        data.setValue(['frontletParameters'], serverResponse.frontletParameters);
       // this.storeData(serverResponse);
        this.setTitle(serverResponse);
        app.currentResponse = serverResponse;
        return serverResponse;
    }

    setTitle(response) {
        if (isSet(response.annotatedTitle)) {  //"" should remove title
          const titleList = document.getElementsByTagName('title');
          if (titleList.length > 0) {
              const title = titleList.item(0);
              title.innerText = response.annotatedTitle;
          }
        }
      }

    storeData(response) {
        this.storeLocalStorageData(response.localStorageData);
        this.storeSessionStorageData(response.sessionStorageData);
        this.storeClientStorageData(response.clientStorageData);
        this.storeGlobalVariableData(response.globalVariableData);
        this.storeLocalDatabaseData(response.localDatabaseData);
    }

    storeLocalStorageData(localStorageData) {
        if (!localStorageData) {
            return;
        }
        app.localStorage.saveData(localStorageData);
    }

    storeSessionStorageData(sessionStorageData) {
        if (!sessionStorageData) {
            return;
        }
        app.sessionStorage.saveData(sessionStorageData);
    }

    storeClientStorageData(clientStorageData) {
        if (!clientStorageData) {
            return;
        }
        app.clientStorage.saveData(clientStorageData);
    }

    storeGlobalVariableData(globalVariableData) {
        if (!globalVariableData) {
            return;
        }
        app.globals.saveData(globalVariableData);
    }

    storeLocalDatabaseData(localDatabaseData) {
        if (!localDatabaseData) {
            return;
        }
        // TODO create and configure db
        for (var key of Object.keys(localDatabaseData)) {
            this.localDatabase.setItem(key, localDatabaseData[key]);
        }
    }


    queryToObject(queryString) {
        if (!queryString) {
            return {};
        }
        var query = {};
        var pairs = (queryString[0] === '?' ? queryString.substr(1) : queryString).split('&');
        for (var i = 0; i < pairs.length; i++) {
            var pair = pairs[i].split('=');
            query[decodeURIComponent(pair[0])] = decodeURIComponent(pair[1] || '');
        }
        return query;
    }



}
