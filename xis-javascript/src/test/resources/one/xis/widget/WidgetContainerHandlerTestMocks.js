var refreshData = undefined;

var widgets = {
    getWidget: function(id) {
        var widgetRoot =  document.createElement('div');
        widgetRoot.setAttribute('id', 'widgetRoot');
        return {id: id, root: widgetRoot, attributes: {}};
    }
}

var data = new Data({x:123});

var client = {

    loadPageData: function (pageId, values) {
        return new Promise((resolve, reject) => {
            resolve(dataResponse);
        });
    }
}