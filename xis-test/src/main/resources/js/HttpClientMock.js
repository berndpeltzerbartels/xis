class HttpClient {


    constructor(frontendServiceWrapper, testRequestFactory) {
        this.frontendServiceWrapper = frontendServiceWrapper;
        this.testRequestFactory = testRequestFactory;
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
            case '/xis/config': return this.frontendServiceWrapper.getComponentConfig();
            case '/xis/page/head': return this.frontendServiceWrapper.getPageHead(headers['uri']);
            case '/xis/page/body': return this.frontendServiceWrapper.getPageBody(headers['uri']);
            case '/xis/page/body-attributes': this.frontendServiceWrapper.getBodyAttributes(headers['uri']);
            default:
                if (uri.startsWith('/xis/widget/html/')) {
                    var id = uri.subtring('/xis/widget/html/'.length);
                    return this.frontendServiceWrapper.getWidgetHtml(id);
                }
                throw new Error('unknown uri for http-get: ' + uri);
        }
    }

    responseForPost(uri, payload, headers) {
        var request = this.createRequest(payload, headers);
        switch (uri) {
            case '/xis/page/model': return this.frontendServiceWrapper.getPageModel(request);
            case '/xis/widget/model': return this.frontendServiceWrapper.getWidgetModel(request);
            case '/xis/page/action': return this.frontendServiceWrapper.invokePageActionMethod(request);
            case '/xis/widget/action': return this.frontendServiceWrapper.invokeWidgetActionMethod(request);
            default: throw new Error('unknown uri for http-post: ' + uri);
        }
    }

    createRequest(payload, headers) {
        return this.testRequestFactory.createRequest(uri, payload, headers);
    }

}
