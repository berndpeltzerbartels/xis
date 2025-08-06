var app;

function main() {
    app = new Application();
    app.start();

    // backward-button
    window.addEventListener('popstate', event => app.history.onPopState(event));

}


window.main = main;