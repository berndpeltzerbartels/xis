var refreshData = undefined;

var widgets = {
    getWidget: function(id) {
        var widgetRoot =  document.createElement('div');
        widgetRoot.setAttribute('id', 'widgetRoot');
        widgetRoot._refresh = data => {refreshData = data};
        return {id: id, root: widgetRoot, attributes: {}, _refresh: function(data){refreshData=dfata; }};
    }
}

var data = new Data({});

var client = {

    loadPageData: function (pageId, values) {
        return new Promise((resolve, reject) => {
            resolve(dataResponse);
        });
    }
}