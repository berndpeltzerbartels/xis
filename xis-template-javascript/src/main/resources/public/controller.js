
class Controller {

    /**
     * 
     * @param {Client} client 
     */
    constructor(client) {
        this.client = client;
        this.widgetDoms = {};
        this.pageHeadContent = {};
        this.pageBodyContent = {};
        this.pageBodyAttributes = {};
        this.pageData = {};
        this.headChildNodes = [];
        this.welcomePageId;
    }

    init() {
        var controller = this;
        this.loadConfig().then(config => {
            controller.welcomePageId = config.welcomePageId;
            var promises = [];
            config.widgetIds.forEach(id => promises.push(controller.loadWidget(id)));
            config.pageIds.forEach(id => promises.push(controller.loadPageHead(id)));
            config.pageIds.forEach(id => promises.push(controller.loadPageBody(id)));
            config.pageIds.forEach(id => promises.push(controller.loadPageBodyAttributes(id)));
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
            var head = controller.pageHeadContent[pageId];
            var body = controller.pageBodyContent[pageId];
            var attributes = controller.pageBodyAttributes[pageId];
            bindHead(head);
            bindBody(body, attributes);
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
            if (!controller.pageHeadContent[uri]) {
                uri = controller.welcomePageId;
            }
            resolve(uri);
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
            var head = controller.pageHeadContent[pageId];
            var body = controller.pageBodyContent[pageId];
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
     * @param {string} pageId
    * @returns {Promise<string>}
    */
    loadPageHead(pageId) {
        var controller = this;
        return this.client.loadPageHead(pageId).then(content => {
            controller.pageHeadContent[pageId] = content;
            return pageId;
        });
    }

    /**
   * @param {string} pageId
  * @returns {Promise<string>}
  */
    loadPageBody(pageId) {
        var controller = this;
        return this.client.loadPageBody(pageId).then(content => {
            controller.pageBodyContent[pageId] = content;
            return pageId;
        });
    }

    loadPageBodyAttributes(pageId) {
        var controller = this;
        return this.client.loadPageBodyAttributes(pageId).then(attributes => {
            controller.pageBodyAttributes[pageId] = attributes;
            return pageId;
        });
    }
    /**
    * @returns {Promise<string>}
    */
    loadWidget(widgetId) {
        var controller = this;
        return this.client.loadWidget(widgetId).then(html => {
            controller.widgetDoms[widgetId] = controller.asRootElement(html);
            return widgetId;
        });
    }


    /**
    * @returns {Promise<ComponentConfig>}
    */
    loadConfig() {
        return this.client.loadConfig();
    }

    /**
     *
     * @param {string} tree
     * @returns {Element}
     */
    asRootElement(tree) {
        var div = document.createElement('div');
        div.innerHTML = trim(tree);
        return div.childNodes.item(0);
    }

}


