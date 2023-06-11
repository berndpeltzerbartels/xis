var div1 = document.createElement('div');
var div2 = document.createElement('div');
div1.appendChild(div2);

var titleElement = document.createElement('title');
titleElement.innerText = 'Test'

var headChildArray = [document.createElement('style'), document.createElement('script'), titleElement];
var bodyChildArray = [div1];


var page = {
    id : 'index.html',
    headChildArray: headChildArray,
    bodyChildArray: bodyChildArray,
    bodyAttributes: { class: 'test' },
    title: 'Test'
};


var dataResponse = {
    data: {
        test: 123
    }
};

var config = {
    welcomePageId: 'index.html',
    pageAttributes : {
        'index.html' : {
            modelsToSubmitForModel: []
        }
    }
}


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