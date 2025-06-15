class HttpConnectorMock {

    /**
     * @public
     * @param {string} uri
     * @param {any} payload
     * @param {any} headers
     * @return {Promise<any,int>}
     *
     */
    post(uri, payload, headers) {
        this.logRequest(uri, payload, headers);
        return new Promise((resolve, reject) => {
            console.log('----------------------------------response------------------------------------');
            var response = this.responseForPost(uri, payload, headers);
            this.logResponse(response);
            resolve(response);
        });
    }

    /**
     * @public
     * @param {string} uri
     * @param {any} headers
     * @return {Promise<any, int>}
     */
    get(uri, headers) {
        this.logRequest(uri, {}, headers);
        return new Promise((resolve, reject) => {
            var response = this.responseForGet(uri, headers);
            this.logResponse(response);
            resolve(response);
        });
        console.log('----------------------------------------------------------------------');
    }

    responseForPost(uri, payload, headers) {
        var requestJson = JSON.stringify(payload);
        switch (uri) {
            case '/xis/page/model': return backendBridge.getPageModel(uri, requestJson, headers);
            case '/xis/widget/model': return backendBridge.getWidgetModel(uri, requestJson, headers);
            case '/xis/form/model': return backendBridge.getFormModel(uri, requestJson, headers);
            case '/xis/page/action': return backendBridge.onPageLinkAction(uri, requestJson, headers);
            case '/xis/widget/action': return backendBridge.onWidgetLinkAction(uri, requestJson, headers);
            case '/xis/form/action': return backendBridge.onFormAction(uri, requestJson, headers);
            case '/xis/token-provider/login': return backendBridge.localTokenProviderLogin(uri, requestJson, headers);
            default: throw new Error('unknown uri for http-post: ' + uri);
        }
    }

    responseForGet(uri, headers) {
        switch (uri) {
            case '/xis/config': return backendBridge.getComponentConfig(uri, headers);
            case '/xis/page': return backendBridge.getPage(uri, headers);
            case '/xis/page/head': return backendBridge.getPageHead(uri, headers);
            case '/xis/page/body': return backendBridge.getPageBody(uri, headers);
            case '/xis/page/body-attributes': return backendBridge.getBodyAttributes(uri, headers);
            case '/xis/widget/html': return backendBridge.getWidgetHtml(uri, headers);
            case '/xis/token-provider/tokens': return backendBridge.localTokenProviderGetTokens(uri, headers);
            case '/xis/token/renew': return backendBridge.renewApiTokens(uri, headers);
            default: throw new Error('unknown uri for http-get: ' + uri);
        }
    }

    logRequest(uri, payload, headers) {
        console.log('---------------------------------request-------------------------------------');
        for (var key in headers) {
            console.log('header: ' + key + ' : ' + headers[key]);
        }
        console.log('post: ' + uri + ' : ' + JSON.stringify(payload || {}));
    }

    logResponse(response) {
        console.log('----------------------------------response------------------------------------');
        console.log('status: ' + response.status);
        // log headers
        console.log('headers:', response.getAllResponseHeaders());
        console.log('response: ' + response);
    }
}


