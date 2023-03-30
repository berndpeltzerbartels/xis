


var client = new Client(new HttpClient());
var widgets = new Widgets(client);
var treeRefresher = new TreeRefresher(client, widgets);
var pageController = new PageController(treeRefresher, client);
var nodeCloner = new NodeCloner();
var initializer = new Initializer(nodeCloner);
var starter = new Starter(pageController, widgets, client);
starter.doStart();