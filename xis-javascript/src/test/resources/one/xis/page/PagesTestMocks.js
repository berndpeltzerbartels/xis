var client = {

    calledMethods: [],

    loadPageHead: function (pageId) {
        this.calledMethods.push('loadPageHead');
        return new Promise((resolve, reject) => {
            resolve(`<head>
                        <title>Title</title>
                        <style>

                        </style>
                      </head>
            `)
        });
    },

    loadPageBody: function (pageId) {
        this.calledMethods.push('loadPageBody');
        return new Promise((resolve, reject) => {
            resolve(`<body>
                        <h5>Title</h5>
                        <div>

                        </div>
                    </body>
            `)
        });
    },

    loadPageBodyAttributes: function (pageId) {
        this.calledMethods.push('loadPageBodyAttributes');
        return new Promise((resolve, reject) => {
            resolve('{"class": "test"}');
        });
    }
}


var initializer = {
    initializedNodes:[],
    initialize: function (node) {
        this.initializedNodes.push(node);
    }
}


var config =  {
    pageIds: ['index.html']
}
