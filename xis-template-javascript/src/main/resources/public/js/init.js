var root = getTemplateRoot();
if (!root._xis) root._xis = {};

var client = new Client(new HttpClient());
var controller = new PageController(client);
controller.init();