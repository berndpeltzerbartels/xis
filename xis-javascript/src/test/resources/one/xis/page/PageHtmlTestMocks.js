

var title = document.createElement('title');
title.innerText = 'Page'

var h1 = document.createElement('h1')
h1.innerText = 'Page'

var div = document.createElement('div');
div.innerText = 'Page'

var style = document.createElement('style');
var script = document.createElement('script');
var page = {
    normalizedPath: '/page.html',
    headChildArray: [title, style, script],
    bodyChildArray: [h1, div],
    bodyAttributes: { class: 'Page' },
    titleExpression: new TextContentParser('Page').parse()
};
