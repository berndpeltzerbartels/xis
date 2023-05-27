

class Starter {

    /**
     *
     * @param {HttpClient} httpClient
     */
    constructor(httpClient) {
        this.domAccessor = new DomAccessor();
        this.client = new Client(httpClient);
        this.pages = new Pages(this.client);
        this.widgetContainers = new WidgetContainers();
        this.widgets = new Widgets(this.client);
        this.initializer = new Initializer(this.domAccessor, this.client, this.widgets, this.widgetContainers);
        this.pageController = new PageController(this.client, this.pages, this.initializer);
    }

    doStart() {
        var head = getElementByTagName('head');
        var body = getElementByTagName('body');
        this.initializer.initializeHtmlElement(head);
        this.initializer.initializeHtmlElement(body);
        console.log('Loading configuration');
        var _this = this;
        this.loadConfig()
            .then(config => _this.widgets.loadWidgets(config))
            .then(config => _this.pages.loadPages(config))
            .then(config => _this.pageController.displayInitialPage(config))
            .catch(e => console.log(error));
    }
    /**
    * @returns {Promise<ComponentConfig>}
    */
    loadConfig() {
        return this.client.loadConfig();
    }
}


