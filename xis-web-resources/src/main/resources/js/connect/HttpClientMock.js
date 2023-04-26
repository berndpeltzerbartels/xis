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
        // console.log('post: ' + uri + ' : ' + JSON.stringify(payload));
        var _this = this;
        return new Promise((resolve, reject) => {
            // console.log('resolve post: ' + uri + ' : ' + JSON.stringify(payload));
            var response = this.responseForPost(uri, payload, headers);
            //console.log('response for post: ' + uri + ': ' + JSON.stringify(response));
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
        //console.log('get: ' + uri);
        var _this = this;
        return new Promise((resolve, reject) => {
            //console.log('resolve get: ' + uri);
            var response = _this.responseForGet(uri, headers);
            //console.log('response for get: ' + uri + ': ' + JSON.stringify(response));
            resolve(response);
        });
    }

    responseForGet(uri, headers) {
        switch (uri) {
            case '/xis/config': return this.controllerBridge.getComponentConfig(uri, headers);
            case '/xis/page/head': return this.controllerBridge.getPageHead(uri, headers);
            case '/xis/page/body': return this.controllerBridge.getPageBody(uri, headers);
            case '/xis/page/body-attributes': return this.controllerBridge.getBodyAttributes(uri, headers);
            default:
                if (uri.startsWith('/xis/widget/html/')) {
                    return this.controllerBridge.getWidgetHtml(uri, headers);
                }
                throw new Error('unknown uri for http-get: ' + uri);
        }
    }

    responseForPost(uri, payload, headers) {
        var requestJson = JSON.stringify(payload);
        switch (uri) {
            case '/xis/page/model': return this.controllerBridge.getPageModel(uri, requestJson, headers);
            case '/xis/widget/model': return this.controllerBridge.getWidgetModel(uri, requestJson, headers);
            case '/xis/page/action': return this.controllerBridge.invokePageActionMethod(uri, requestJson, headers);
            case '/xis/widget/action': return this.controllerBridge.invokeWidgetActionMethod(uri, requestJson, headers);
            default: throw new Error('unknown uri for http-post: ' + uri);
        }
    }

}
