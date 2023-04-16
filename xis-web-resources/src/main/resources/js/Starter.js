

class Starter {

    /**
     *
     * @param {HttpClient} httpClient
     */
    constructor(httpClient) {
        this.domAccessor = new DomAccessor();
        this.initializer = new Initializer(this.domAccessor);
        this.client = new Client(httpClient);
        this.widgetService = new WidgetService(this.client);
        this.pages = new Pages(this.client, this.initializer);
        this.pageController = new PageController(this.client, this.pages);
    }

    doStart() {
        console.log('Loading configuration');
        var _this = this;
        this.loadConfig()
            .then(config => _this.widgetService.loadWidgets(config))
            .then(config => _this.pages.loadPages(config))
            .then(config => _this.pageController.displayInitialPage(config));
    }
    /**
    * @returns {Promise<ComponentConfig>}
    */
    loadConfig() {
        return this.client.loadConfig();
    }
}
