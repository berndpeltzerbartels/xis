class HttpConnectorMock {

    /**
     * @public
     * @param {string} uri
     * @param {any} payload
     * @return {Promise<any,int>}
     *
     */
    post(uri, payload, headers) {
        console.log('HTTP - POST: ' + uri + ' : ' + JSON.stringify(payload));
        var _this = this;
        return new Promise((resolve, reject) => {
            var response = _this.responseForPost(uri, payload, headers);
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
        console.log('HTTP - GET: ' + uri);
        var _this = this;
        return new Promise((resolve, reject) => {
            var response = _this.responseForGet(uri, headers);
            resolve(response);;
        });
    }

    responseForGet(uri, headers) {
        switch (uri) {
            case '/xis/config': return backendBridge.getComponentConfig(uri, headers);
            case '/xis/page/head': return backendBridge.getPageHead(uri, headers);
            case '/xis/page/body': return backendBridge.getPageBody(uri, headers);
            case '/xis/page/body-attributes': return backendBridge.getBodyAttributes(uri, headers);
            case '/xis/widget/html': return backendBridge.getWidgetHtml(uri, headers);
            default: throw new Error('unknown uri for http-get: ' + uri);
        }
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
            default: throw new Error('unknown uri for http-post: ' + uri);
        }
    }

}
