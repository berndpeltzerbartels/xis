

class Starter {

    /**
     *
     * @param {HttpClient} httpClient
     */
    constructor(httpClient) {
        this.client = new Client(httpClient);
        this.widgetService = new WidgetService(this.client);
        this.pageController = new PageController(this.client, this.pages);
        this.domAccessor = new DomAccessor();
        this.initializer = new Initializer(this.domAccessor);
        this.pages = new Pages(this.client, this.initializer);
    }

    doStart() {
        var _this = this;
        new RootPageInitializer().initialize();
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
