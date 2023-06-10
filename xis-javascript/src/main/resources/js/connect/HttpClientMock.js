class HttpClientMock {


    constructor(backendBridgeProvider) {
        this.backendBridgeProvider = backendBridgeProvider;
    }

    /**
     * @public
     * @param {string} uri
     * @param {any} payload
     * @return {Promise<any>}
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
     * @return {Promise<any>}
     */
    get(uri, headers) {
        console.log('HTTP - GET: ' + uri);
        var _this = this;
        return new Promise((resolve, reject) => {
            var response = _this.responseForGet(uri, headers);
            resolve(response);
        });
    }

    responseForGet(uri, headers) {
        var backendBridge = this.backendBridgeProvider.getBackendBridge();
        switch (uri) {
            case '/xis/config': return backendBridge.getComponentConfig(uri, headers);
            case '/xis/page/head': return backendBridge.getPageHead(uri, headers);
            case '/xis/page/body': return backendBridge.getPageBody(uri, headers);
            case '/xis/page/body-attributes': return backendBridge.getBodyAttributes(uri, headers);
            default:
                if (uri.startsWith('/xis/widget/html/')) {
                    return backendBridge.getWidgetHtml(uri, headers);
                }
                throw new Error('unknown uri for http-get: ' + uri);
        }
    }

    responseForPost(uri, payload, headers) {
        var backendBridge = this.backendBridgeProvider.getBackendBridge();
        var requestJson = JSON.stringify(payload);
        switch (uri) {
            case '/xis/page/model': return backendBridge.getPageModel(uri, requestJson, headers);
            case '/xis/widget/model': return backendBridge.getWidgetModel(uri, requestJson, headers);
            case '/xis/page/action': return backendBridge.onPageAction(uri, requestJson, headers);
            case '/xis/widget/action': return backendBridge.onWidgetAction(uri, requestJson, headers);
            default: throw new Error('unknown uri for http-post: ' + uri);
        }
    }

}
