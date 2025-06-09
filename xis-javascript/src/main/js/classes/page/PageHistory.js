class PageHistory {
    /**
     * @param {PageController} pageController
     */
    constructor(pageController) {
        this.pageController = pageController;
        this.history = [];
        this.currentIndex = -1;
    }
    
    /**
     * Adds a page to the history.
     * @param {string} resolvedURL
     */
    appendPage(resolvedURL, title) {
        // Remove all pages after the current index
        this.history = this.history.slice(0, this.currentIndex + 1);
        // Add the new page
        this.history.push({resolvedURL: resolvedURL, title: title});
        this.currentIndex++;
        
        // Update the browser history
       window.history.pushState({ index: this.currentIndex }, title, resolvedURL.url);
    }

    onPopState(event) {
        debugger;
        if (event.state && event.state.index !== undefined) {
            this.currentIndex = event.state.index;
            if (this.currentIndex < 0 || this.currentIndex >= this.history.length) {
                console.warn('Invalid history index:', this.currentIndex);
                return;
            }
            const item = this.history[this.currentIndex];
            if (!item) {
                console.warn('No item found in history for index:', this.currentIndex);
                return;
            }
            this.pageController.displayPageForUrl(item.resolvedURL.url, {skipHistoryUpdate: true}); // Skip updating history to avoid infinite loop
            const titleElement = document.querySelector('title');
            if (titleElement) {
                titleElement.innerText = item.title; // Update the document title
            } else {
                console.warn('No title element found to update');
            }
        } else {
            console.warn('No state found in popstate event');
        }
    }

    /**
     *  Returns current page in the history and title or null if there is no current page.
     * 
     * @public
     * @returns {ResolvedURL,string}
     */
    currentItem() {
        if (this.currentIndex < 0 || this.currentIndex >= this.history.length) {
            return null;
        }
        return this.history[this.currentIndex];
    }


}