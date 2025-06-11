class HttpConnector {

    /**
     * @param {Function} errorHandler
     */
    constructor(errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * @public
     * @param {string} uri
     * @param {any} payload
     * @return {Promise<any, int>}
     *
     */
    post(uri, payload, headers) {
        if (!headers) headers = {};
        var payloadJson = JSON.stringify(payload);
        headers['Content-type'] = 'application/json';
        return this.doRequest(uri, headers, 'POST', payloadJson);
    }

    /**
     * @public
     * @param {string} uri
     * @param {any} headers
     * @return {Promise<any, int>}
     */
    get(uri, headers) {
        return this.doRequest(uri, headers, 'GET', undefined);
    }

    /**
     * Sends a request to renew the access token using the provided renew token.
     * This method is typically used when the access token has expired and needs to be refreshed.
     * It sends a POST request to the '/xis/token/renew' endpoint with the renew token in the request body.
     * The response is expected to contain new tokens in the Set-Cookie header.
     * 
     * @public
     * @param {string} renewToken 
     * @returns {Promise<Tokens>}
     * @throws {Error} If the request fails or the response does not contain the expected token data.
     */
    sendRenewTokenRequest(renewToken) {
        return this.httpConnector.post('/xis/token/renew', { renewToken: renewToken }, {'Authetication': 'Bearer ' + renewToken})
            .then(response => this.readTokenData(response));
    }

    

    /**
     * the repsonse contains tokens as cookies like this:
     * "access_token", tokenResponse.getAccessToken()) +
                        "; HttpOnly; Secure; Path=/; SameSite=Strict; Max-Age="

                        This method reads the token data from the response.
     * @param {Tokens} response 
     * @returns 
     */
    readTokenData(response) {
        if (response.status == 200) {
            const tokens = new Tokens();
            const cookies = response.getResponseHeader('Set-Cookie');
            if (cookies) {
                const cookieArray = cookies.split(';');
                for (const cookie of cookieArray) {
                    const [name, value] = cookie.trim().split('=');
                    if (name === 'access_token') {
                        tokens.token = value;
                    } else if (name === 'access_token_expires_at') {
                        tokens.accessTokenExpiresAt = parseInt(value, 10);
                    } else if (name === 'renew_token') {
                        tokens.renewToken = value;
                    } else if (name === 'renew_token_expires_at') {
                        tokens.renewTokenExpiresAt = parseInt(value, 10);
                    }
                }
                return tokens;
            }
           throw Error('No Set-Cookie header found in the response.');
        }
        return null;
    }

    /**
     * @private
     * @param {string} uri
     * @param {any} headers
     * @param {string} method
     * @param {any} payload
     * @return {Promise<any>}
     */
    doRequest(uri, headers, method, payload) {
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.open(method, uri, true); // true for asynchronous
        for (var name of Object.keys(headers)) {
            xmlHttp.setRequestHeader(name, headers[name]);
        }
        var promise = new Promise((resolve, reject) => {
            xmlHttp.onreadystatechange = function () {
                // TODO Handle errors and "304 NOT MODIFIED"
                // TODO Add headers to allow 304
                // Readystaet == 4 for 304 ?
                if (xmlHttp.readyState == 4) { // TODO In Java 204 if there is no server-method
                    resolve(xmlHttp);
                }
                // TODO use errorhandler
            }

        });

        if (payload) {
            xmlHttp.send(payload);
        }
        else {
            xmlHttp.send();
        }
        return promise;
    }

}
