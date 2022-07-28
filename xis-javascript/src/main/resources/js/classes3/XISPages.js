class XISPages {

    constructor() {
        this.pages = {};
        this.welcomePage = undefined;
    }


    addPage(key, page) {
        this.pages[key] = page;
    }

    getPage(key) {
        return this.pages[key];
    }

    setWelcomePage(key) {
        this.welcomePage = this.pages[key];
    }

    /**
     * 
     * @param {string} uri 
     */
    getPage(uri) {
        if (uri.startsWith('/')) {
            uri = uri.substring(1);
        }

        if (uri.endsWith('.html')) {
            uri = uri.substring(0, uri.length - 5);
        }
        var key = uri.replace('/', ':');
        if (this.pages[key]) {
            return this.pages[key];
        }
        return this.welcomePage;
    }
        
}