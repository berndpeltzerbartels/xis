var client = {
    loadWidget: function(id) {
         return new Promise((resolve, reject) => {
            resolve(`<h5>Widget</h5>
                    <div>

                    </div>
            `)
         });
    }
}

var config = {
    widgetIds: ['widgetId'],
    widgetAttributes: {
        'widgetId': {}
    }
}