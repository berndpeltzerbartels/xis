class XISPages {

    constructor() {
        this.className = 'XISPages';
        this.pages = {};
        this.welcomePage = undefined;
    }


    addPage(key, page) {
        this.pages[key] = page;
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
       
        var page = this.pages[key] ? this.pages[key] : this.welcomePage;
        if (!page.initialized){
            page.init();
            page.initialized = true;        
        }
        return page;
    }
        
}