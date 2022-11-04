class XISHttpClient  {
    
    /**
     * @param {Function} errorHandler 
     */
    constructor(errorHandler) {
      this.errorHandler = errorHandler;      
    }

    get(uri, headers, parameters, handler) {
        var address = uri;
        if (parameters) {
            address += '?'
            for(var name of Object.keys(parameters))  {
                address += name;
                address += '=';
                address += encodeURI(parameters[name]);
            }
        }
       this.doRequest(address, headers, 'GET', null, handler); 
    }

    post(uri, headers, payload, handler) {
        var payloadJson = JSON.stringify(payload);
        headers['Content-length'] = payloadJson.length;
        this.doRequest(uri, headers, 'POST', payloadJson, handler); 
    }

    /**
     * @private
     * @param {string} uri 
     * @param {any} headers
     * @param {string} method 
     * @param {any} payload 
     * @param {Function} handler 
     */
    doRequest(uri, headers, method, payload, handler) {
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.open(method, uri, true); // true for asynchronous
        for (var name of Object.keys(headers)) {
            xmlHttp.setRequestHeader(name, headers[name]);
        }
        xmlHttp.onreadystatechange = function() { 
            // TODO Handle errors and "304 NOT MODIFIED"
            // TODO Add headers to allow 304
            // Readystaet == 4 for 304 ?
            if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
                handler(JSON.parse(xmlHttp.responseText));
            }
            // TODO use errorhandler
        }
        xmlHttp.send(payload);
    }

    
}