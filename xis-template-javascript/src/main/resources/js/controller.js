
class Controller {

    /**
     * 
     * @param {Client} client 
     */
    constructor(client) {
        this.client = client;
        this.widgetDoms = {};
        this.pageHeadDoms = {};
        this.pageBodyDoms = {};
        this.pageData = {};
        this.headChildNodes = [];
        this.welcomePageId;
    }

    init() {
        var controller = this;
        var client = this.client;
        client.loadConfig().then(config => {
            var promises = [];
            config.widgetIds.forEach(id => promises.push(controller.loadWidget(id)));
            config.pageIds.forEach(id => promises.push(controller.loadPage(id)));
            return Promise.all(promises);

        }).then(() => controller.findPageId())
            .then(pageId => controller.displayPage(pageId));
    }


    /**
   * @returns {Promise<string>}
   */
    displayPage(pageId) {
        var controller = this;
        return new Promise((resolve, _) => {
            var head = controller.pageHeadDoms[pageId];
            var body = controller.pageBodyDoms[pageId];
            bindHead(head);
            bindBody(body);
            resolve(pageId);
        });
    }

    pageAction(pageId, action) {
        var controller = this;
        return this.client.pageAction(pageId, action, this.pageData[pageId]).then(response => {
            var data = response.data;
            for (var key of Object.keys(data)) {
                controller.pageData[pageId][key] = data[key];
            }
            return response.nextControllerId;
        }).then(controllerId => controller.displayPage(controllerId));
    }


    /**
     * @returns {Promise<string>}
     */
    findPageId() {
        var controller = this;
        return new Promise((resolve, _) => {
            var uri = document.location.pathname;
            if (!controller.pageHeadDoms[uri]) {
                uri = controller.welcomePageId;
            }
            resolve(uri);
        });
    }




    /**
     * @returns {Promise<string>}
     */
    loadData(pageId) {
        var controller = this;
        return new Promise((resolve, _) => {
            var head = controller.pageHeadDoms[id];
            var body = controller.pageBodyDoms[id];
            bindHead(head);
            bindBody(body);
            resolve(pageId);
        });
    }


    /**
     * @returns {Promise<string>}
     */
    refreshData(pageId) {
        var controller = this;
        return client.loadPageData(pageId, controller.pageData[pageId] || {}).then(response => {
            var data = response.data;
            for (var key of Object.keys(data)) {
                controller.pageData[pageId][key] = data[key];
            }
            return pageId;
        });
    }

    /**
    * @returns {Promise<string>}
    */
    refreshPage(pageId) {
        var controller = this;
        return new Promise((resolve, _) => {
            var head = controller.pageHeadDoms[pageId];
            var body = controller.pageBodyDoms[pageId];
            var data = controller.pageData[pageId];
            refresh(head, data);
            refresh(body, data);
            controller.updateHistory(head);
            resolve(pageId);
        });
    }

    updateHistory(head) {
        var titleList = head.getElementsByTagName('title');
        var title = titleList.length > 0 ? titleList.item(0).innerHTML : '';
        window.history.pushState({}, title, pageId);
    }

    /**
     * @param {string} id 
     * @returns {Element}
     */
    getWidgetDom(id) {
        return this.widgetDoms[id];
    }

    /**
    * @returns {Promise<string>}
    */
    loadPage(pageId) {
        var controller = this;
        this.client.loadHead(pageId).then(html => {
            var root = controller.asRootElement(html);
            controller.pageHeadDoms[pageId] = root.getElementsByTagName('head');
            controller.pageBodyDoms[pageId] = root.getElementsByTagName('body');
            return pageId;
        });
    }


    /**
    * @returns {Promise<string>}
    */
    loadWidget(widgetId) {
        var controller = this;
        this.client.loadBody(pageId).then(html => {
            controller.widgetDoms[pageId] = controller.asRootElement(html);
            return widgetId;
        });
    }

    /**
     * 
     * @param {string} tree 
     * @returns {Element}
     */
    asRootElement(tree) {
        var div = document.createElement('div');
        div.innerHTML = tree;
        return div.childNodes.item(0);
    }

}


