var app;

function main() {
    app = new Application();
    app.start();

    // backward-button
    window.addEventListener('popstate', function (event) {
        if (event.state) {
            app.pageController.displayPageForUrl(event.target.location.pathname);
        }
    });

}
