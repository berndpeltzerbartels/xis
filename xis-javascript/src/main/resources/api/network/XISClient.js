class XISClient {

    /**
     * @param {XISErrorHandler} errorHandler 
     */
    constructor(localStorage, errorHandler) {
        this.className = 'XISClient';
        this.restClient = new XISRestClient(errorHandler);
        this.token = localStorage.getItem('xis-token');
        this.clientId = localStorage.getItem('xis-client-id');
        if (!this.clientId) {
            this.clientId = randomString(12);
            localStorage.setItem('xis-client-id', this.clientId);
        }
        this.messageStack = [];
        this.mesageComponents = [];
    }


    sendPhaseMessage(phase, component, data, parameters, callback) {

    }



    addActionMessage(component, action, data, parameters) {
        this.mesageComponents.push(component);
        this.messageStack.push({
            componentClass: component.className,
            componentType: component.type,
            type: 'action',
            data: data,
            actionKey: action,
            parameters: parameters
        });
    }

    addPhaseMessage(component, phase, data, parameters) {
        this.mesageComponents.push(component);
        this.messageStack.push({
            componentClass: component.className,
            componentType: component.type,
            type: 'phase',
            data: data,
            phase: phase,
            componentParameters: parameters
        });
    }

    submit() {
        var client = this;
        var request = {
            messages: client.messageStack,
            urlParameters: client.getUrlParameters(),
            timestamp: new Date()
        };
        this.restClient.post('/xis/ajax', this.getRequestHeaders(), request, response => {
            for (var i = 0; i < this.messageStack.length; i++) {
                var message = response.messages[i];
                client.mesageComponents[i].processResponse(message.componentState);
            }
        });
        this.mesageComponents = {};
        this.messageStack = {};
    }

    getUrlParameters() {
        return {};// TODO
    }

    /**
     * @private
     * @returns {any}
     */
    getRequestHeaders() {
        return {
            clientId: client.clientId,
            token: client.token,
            timestamp: new Date()
        }
    }
    /**
     * TODO remove following:
     * @param {XISPage} page 
     */
    sendPageModelRequest(page) {
        this.sendModelRequest(page, '/xis/ajax/page/model');
    }

    /**
     * 
     * @param {XISWidget} widget 
     */
    sendWidgetRequest(widget) {
        this.sendModelRequest(widget, '/xis/ajax/page/model');
    }


    /**
     * 
     * @param {XISPage} page 
     * @param {*} action 
     */
    sendPageActionRequest(page, action) {
        this.sendActionRequest(page, action, '/xis/ajax/page/action');
    }

    /**
     * 
     * @param {XISWidget} widget 
     * @param {String} action 
     */
    sendWidgetActionRequest(widget, action) {
        this.sendActionRequest(widget, action, '/xis/ajax/widget/action');
    }


    /**
     * @private
     * @param {XISComponent} component 
     * @param {String} url 
     */
    sendModelRequest(component, url) {
        var request = this.createBasicRequest(component);
        request.clientState = this.filteredClientState(component.initClientKeys);
        this.restClient.post(url, request, response => {
            component.processData(response.componentState);
            clientState.processData(response.clientState);
        });

    }

    /**
     * @private
     * @param {XISComponent} component 
     * @param {String} action 
     * @param {String} url 
     */
    sendActionRequest(component, action, url) {
        var request = this.createBasicRequest(component);
        request.clientState = this.filteredClientState(component.actionClientKeys[action]);
        request.componentState = this.filteredComponentState(component.actionCompKeys[action]);
        this.restClient.post(url, request, response => {
            component.processData(response.componentState);
            clientState.processData(response.clientState);
            if (response.nextComponent) {
                component.replace(response.nextComponent);
            }
        });
    }

    /**
     * @private
     * @param {XISComponent} component 
     * @returns {any}
     */
    createBasicRequest(component) {
        return {
            model: component.state,
            clientId: this.clientId,
            token: this.token,
            controllerClass: component.controllerClass,
            componentId: component.componentId,
            timestamp: new Date(),
        };
    }

    isEmpty(obj) {
        return Object.keys(obj).length == 0;
    }


    /**
     * @private
     * @param keys {array}
     * @returns {any} 
     */
    filteredClientState(keys) {
        var state = {};
        for (key in keys) {
            state[key] = clientState.getValue(key);
        }
        return state;
    }


    /**
     * @private
     * @param {XISComponent} component
     * @param {array} keys 
     * @returns {any} 
     */
    filteredComponentState(component, keys) {
        var state = {};
        for (key in keys) {
            state[key] = component.getValue(key);
        }
        return state;
    }
}