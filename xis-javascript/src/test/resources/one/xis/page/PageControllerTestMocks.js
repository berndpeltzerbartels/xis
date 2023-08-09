var titleWelcomePage = document.createElement('title');
titleWelcomePage.innerText = 'WelcomePage';

var h1WelcomePage = document.createElement('h1')
h1WelcomePage.innerText = 'WelcomePage';

var welcomePage = {
    normalizedPath: '/index.html',
    headChildArray: [titleWelcomePage],
    bodyChildArray: [h1WelcomePage],
    bodyAttributes: { class: 'WelcomePage' },
    titleExpression: new TextContentParser('WelcomePage').parse()
};

var welcomePagePath = new Path(new PathElement({ type: 'static', content: '/index.html' }));



var title = document.createElement('title');
title.innerText = 'Page'

var h1 = document.createElement('h1')
h1.innerText = 'Page'

var div = document.createElement('div');

var page = {
    normalizedPath: '/page.html',
    headChildArray: [title],
    bodyChildArray: [h1, div],
    bodyAttributes: { class: 'Page' },
    titleExpression: new TextContentParser('Page').parse()
};

var pagePath = new Path(new PathElement({ type: 'static', content: '/page.html' }));


var dataResponse = {
    data: new Data({test: 123})
};


var pages = {
    getPage: function (normalizedPath) {
        return page;
    },
    getAllPaths: function () {
        return [welcomePagePath, pagePath];
    },
    getWelcomePage: function () {
        return welcomePage;
    }
};


var config = {
    pageAttributes: {
        '/index.html': { modelsToSubmitOnRefresh: [] },
        '/page.html': { modelsToSubmitOnRefresh: [] }
    }
};

var client = {

    loadPageData: function (pageId, values) {
        return new Promise((resolve, reject) => {
            resolve(dataResponse);
        });
    }
}
