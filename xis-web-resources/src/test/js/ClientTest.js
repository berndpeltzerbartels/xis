const Client = require('../../main/resources/modules/Client.mjs')
const HttpClient = require('../../main/resources/modules/HttpClient.mjs')


var httpClient = new HttpClient(undefined);
httpClient.doRequest = function() {
    for (var arg of arguments) {
        console.log(arg);
    }
}
