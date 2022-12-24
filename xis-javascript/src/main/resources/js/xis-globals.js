var errorHandler = new XISErrorHandler();
var httpClient = new XISHttpClient(errorHandler);
var restClient = new XISRestClient(httpClient);
var client = new XISClient(localStorage, restClient);
var rootPage = new XISRootPage();
var pages = new XISPages();
var widgets = new XISWidgets();
var clientAttributes = new XISClientAttributes();
var actions = new XISActions();


