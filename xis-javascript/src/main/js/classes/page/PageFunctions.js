
/**
 * @param {string} url 
 * @param {Array<Parameter>}
 * @returns {Promise<void>}
 */

function displayPageForUrl(url, parameters) {
    return app.pageController.displayPageForUrl(url, parameters);
}


function getCurrentPageId() {
    return app.pageController.pageId;
}
