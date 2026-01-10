function initializeSocketClient(app) {
    console.log('Initializing Socket Client');
    const socketClient = new SocketClient();
    const wsUrl = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
    const host = window.location.host;
    socketClient.connect(wsUrl + host + '/xis/websocket').then(() => {
        app.client = socketClient;
        console.log('SocketClient ready and set as app.client');
    }).catch(error => {
        console.error('Failed to connect SocketClient:', error);
    });
};

eventListenerRegistry.addEventListener(EventType.APP_INSTANCE_CREATED, app => initializeSocketClient(app));