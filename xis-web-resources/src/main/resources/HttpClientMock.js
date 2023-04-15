class HttpClientMock {


    constructor(controllerBridge) {
        this.controllerBridge = controllerBridge;
    }

    /**
     * @public
     * @param {string} uri
     * @param {any} payload
     * @return {Promise<any>}
     *
     */
    post(uri, payload, headers) {
        var _this = this;
        return new Promise((resolve, reject) => {
            resolve(_this.responseForPost(uri, payload, headers));
        });
    }

    /**
     * @public
     * @param {string} uri
     * @param {any} headers
     * @return {Promise<any>}
     */
    get(uri, headers) {
        var _this = this;
        return new Promise((resolve, reject) => {
            resolve(_this.responseForGet(uri, headers));
        });
    }

    responseForGet(uri, headers) {
        switch (uri) {
            case '/xis/config': return this.controllerBridge.getComponentConfig(uri, headers);
            case '/xis/page/head': return this.controllerBridge.getPageHead(uri, headers);
            case '/xis/page/body': return this.controllerBridge.getPageBody(uri, headers);
            case '/xis/page/body-attributes': this.controllerBridge.getBodyAttributes(uri, headers);
            default:
                if (uri.startsWith('/xis/widget/html/')) {
                    return this.controllerBridge.getWidgetHtml(uri, headers);
                }
                throw new Error('unknown uri for http-get: ' + uri);
        }
    }

    responseForPost(uri, payload, headers) {
        var requestJson = JSON.tringify(payload);
        switch (uri) {
            case '/xis/page/model': return this.controllerBridge.getPageModel(uri, requestJson, headers);
            case '/xis/widget/model': return this.controllerBridge.getWidgetModel(uri, requestJson, headers);
            case '/xis/page/action': return this.controllerBridge.invokePageActionMethod(uri, requestJson, headers);
            case '/xis/widget/action': return this.controllerBridge.invokeWidgetActionMethod(uri, requestJson, headers);
            default: throw new Error('unknown uri for http-post: ' + uri);
        }
    }

}
