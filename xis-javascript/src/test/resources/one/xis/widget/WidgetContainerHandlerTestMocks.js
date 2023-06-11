var refreshData = undefined;

var data = new Data({x:'123'});

var dataResponse = {
    data: data
}

var client = {

    loadWidgetData: function (pageId, values) {
        return new Promise((resolve, reject) => {
            resolve(dataResponse);
        });
    }


}