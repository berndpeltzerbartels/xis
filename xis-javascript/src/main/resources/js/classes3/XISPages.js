class XISPages {

    constructor() {
        this.pages = {};
    }


    addPage(key, page) {
        this.pages[key] = page;
    }

    getPage(key) {
        return this.pages[key];
    }
        
}