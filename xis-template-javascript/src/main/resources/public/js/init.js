var root = getTemplateRoot();
if (!root._xis) root._xis = {};

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
var pageController = new PageController(client);
var widgets = new Widgets(client);
var containerController = new ContainerController(client, widgets);
var starter = new Starter(pageController, widgets, client);
starter.doStart();