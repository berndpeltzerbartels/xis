class Starter {

    /**
     * 
     * @param {PageController} pageController 
     * @param {Widgets} widgets 
     */
    constructor(pageController, widgets, client) {
        this.pageController = pageController;
        this.widgets = widgets;
        this.client = client;
    }

    doStart() {
        var _this = this;
        this.loadConfig().then(config => _this.widgets.init(config))
            .then(config => _this.pageController.init(config));
    }
    /**
    * @returns {Promise<ComponentConfig>}
    */
    loadConfig() {
        return this.client.loadConfig();
    }
}


var client = new Client(new HttpClient());
var widgets = new Widgets(client);
var treeRefresher = new TreeRefresher(client, widgets);
var pageController = new PageController(treeRefresher, client);
var nodeCloner = new NodeCloner();
var initializer = new Initializer(nodeCloner);
var starter = new Starter(pageController, widgets, client);
starter.doStart();