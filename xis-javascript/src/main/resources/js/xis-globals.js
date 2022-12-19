var errorHandler = new XISErrorHandler();
var httpClient = new XISHttpClient(errorHandler);
var restClient = new XISRestClient(httpClient);
var client = new XISClient(localStorage, restClient);
var rootPage = new XISRootPage();
var pages = new XISPages();
var widgets = new XISWidgets();
var containers = new XISContainers();
var clientAttributes = new XISClientAttributes();
var actions = new XISActions();


