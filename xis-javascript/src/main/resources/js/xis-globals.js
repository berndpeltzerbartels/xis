var errorHandler = new XISErrorHandler();
var restClient = new XISRestClient(errorHander);
var client = new XISClient(restClient);
var rootPage = new XISRootPage(client);
var pages = new XISPages();
var widgets = new XISWidgets();
var containers = new XISContainers();
