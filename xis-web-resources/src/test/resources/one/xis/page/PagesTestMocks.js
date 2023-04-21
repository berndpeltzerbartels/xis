var client = {

    calledMethods: [],

    loadPageHead: function (pageId) {
        this.calledMethods.push('loadPageHead');
        return new Promise((resolve, reject) => {
            resolve(`<title>Title</title>
                    <style>

                    </style>
            `)
        });
    },

    loadPageBody: function (pageId) {
        this.calledMethods.push('loadPageBody');
        return new Promise((resolve, reject) => {
            resolve(`<h5>Title</h5>
                    <div>

                    </div>
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
