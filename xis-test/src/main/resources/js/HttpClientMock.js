class HttpClient {


    constructor(frontendService, testRequestFactory) {
        this.frontendService = frontendService;
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
            case '/xis/config': return this.frontendService.getComponentConfig();
            case '/xis/page/head': return this.frontendService.getPageHead(headers['uri']);
            case '/xis/page/body': return this.frontendService.getPageBody(headers['uri']);
            case '/xis/page/body-attributes': this.frontendService.getBodyAttributes(headers['uri']);
            default:
                if (uri.startsWith('/xis/widget/html/')) {
                    var id = uri.subtring('/xis/widget/html/'.length);
                    return this.frontendService.getWidgetHtml(id);
                }
                throw new Error('unknown uri for http-get: ' + uri);
        }
    }

    responseForPost(uri, payload, headers) {
        var request = this.createRequest(payload, headers);
        switch (uri) {
            case '/xis/page/model': return this.frontendService.getPageModel(request);
            case '/xis/widget/model': return this.frontendService.getWidgetModel(request);
            case '/xis/page/action': return this.frontendService.invokePageActionMethod(request);
            case '/xis/widget/action': return this.frontendService.invokeWidgetActionMethod(request);
            default: throw new Error('unknown uri for http-post: ' + uri);
        }
    }

    createRequest(payload, headers) {
        return this.testRequestFactory.createRequest(uri, payload, headers);
    }

}
