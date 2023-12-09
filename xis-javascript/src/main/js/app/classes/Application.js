class Application {

    constructor() {
        this.httpClient = new HttpClient();
        this.domAccessor = new DomAccessor();
        this.client = new Client(this.httpClient);
        this.pages = new Pages(this.client);
        this.urlResolver = new URLResolver(this.pages);
        this.widgetContainers = new WidgetContainers();
        this.widgets = new Widgets(this.client);
        this.initializer = new Initializer(this.domAccessor, this.client, this.widgets, this.widgetContainers);
        this.pageController = new PageController(this.client, this.pages, this.initializer, this.urlResolver);
    }


    start() {
        var _this = this;
        this.client.loadConfig()
            .then(config => _this.pageController.setConfig(config))
            .then(config => _this.widgets.loadWidgets(config))
            .then(config => _this.pages.loadPages(config))
            .then(() => _this.pageController.displayPageForUrlLater(document.location.pathname))
            .catch(e => console.error(e));

    }

}