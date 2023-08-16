
/**
 * @param {string} url 
 * @returns {Promise<void>}
 */

function displayPageForUrl(url) {
    return app.pageController.displayPageForUrl(url);
}


function getCurrentPageId() {
    return app.pageController.pageId;
}
