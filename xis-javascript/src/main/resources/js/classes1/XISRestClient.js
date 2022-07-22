class XISRestClient {

    /**
     * @param {XISErrorHandler} errorHandler 
     */
    constructor(errorHandler) {
        this.errorHandler = errorHandler;
    }

    get(uri, handler, parameters, handler) {
        var address = uri;
        if (parameters) {
            address += '?'
            for(var name of Object.keys(parameters))  {
                address += name;
                address += '=';
                address += encodeURI(parameters[name]);
            }
        }
       this.doRequest(address, 'GET', null, handler); 
    }

    post(uri, payload, handler) {
        this.doRequest(uri, 'GET', JSON.stringify(payload), handler); 
    }

    doRequest(uri, method, payload, handler) {
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.setRequestHeader("Accept", "application/json");
        xmlHttp.setRequestHeader("Content-Type", "application/json");
        xmlHttp.onreadystatechange = function() { 
            // TODO Handle errors and "304 NOT MODIFIED"
            // TODO Add headers to allow 304
            // Readystaet == 4 for 304 ?
            if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
                handler(JSON.parse(xmlHttp.responseText));
            }
            // TODO use errorhandler
        }
        xmlHttp.open(method, uri, true); // true for asynchronous 
        xmlHttp.send(payload);
    }

}