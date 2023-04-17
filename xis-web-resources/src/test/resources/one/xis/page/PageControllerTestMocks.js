var div1 = document.createElement('div');
var div2 = document.createElement('div');
div1.appendChild(div2);

var headChildArray = [document.createElement('style'), document.createElement('script')];
var bodyChildArray = [div1];


var page = {
    headChildArray: headChildArray,
    bodyChildArray: bodyChildArray,
    bodyAttributes: { class: 'test' }
};


var dataResponse = {
    data: {}
};


var pages = {
    getPageById: function (id) { return page; }
};

var client = {

    loadPageData: function (pageId, values) {
        return new Promise((resolve, reject) => {
            resolve(dataResponse);
        });
    }
}