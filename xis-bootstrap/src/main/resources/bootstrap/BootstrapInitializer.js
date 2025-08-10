function initializeBootstrap(app) {
   app.messageHandler = new BootstrapMessageHandler();
}

app.initializers.push(initializeBootstrap);