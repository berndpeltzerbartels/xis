
/**
 * Hierarchical page-data
 */
class Data {

    /**
     * 
     * @param {any} values 
     */
    constructor(values, parentData = undefined) {
        this.values = values;
        this.parentData = parentData;
    }
    /**
     * @public 
     * @param {Array} path the path of the data value
     * @returns {any}
     */
    getValue(path) {
        var dataNode = this.values;
        for (var i = 0; i < path.length; i++) {
            var key = path[i];
            if (dataNode[key]) {
                dataNode = dataNode[key].value;
            } else {
                dataNode = undefined;
                break;
            }
        }
        if (dataNode === undefined && this.parentData) {
            return this.parentData.getValue(path)
        }
        return dataNode;
    }

    getKeys() {
        return Object.keys(this.values);
    }

    /**
     * @public 
     * @param {String} key 
     * @param {any} value 
     */
    setValue(key, value, timestamp) {
        this.values[key] = { value: value, timestamp: timestamp };
    }
}

class DataItem {

    constructor() {
        this.key = '';
        this.value = undefined;
        this.timestamp = -1;
    }

    getValue(path) {
        var dataNode = this.values;
        for (var i = 1; i < path.length; i++) {
            var key = path[i];
            if (dataNode[key]) {
                dataNode = dataNode[key].value;
            } else {
                dataNode = undefined;
                break;
            }
        }
        return dataNode;
    }

}


/**
 * @property {any} data
 * @property {string} clientId
 * @property {string} userId
 * @property {string} key // action-key or model-key
 * @property {string} controllerId
 * @property {string} type
 */
class ComponentRequest {

    constructor() {
        this.data = {};
        this.clientId = '';
        this.userId = '';
        this.key = '';
        this.controllerId = '';
        this.type = '';
    }
}



/**
 * @property {array<string>} pageIds
 * @property {array<string>} widgetIds
 * @property {any} pageHosts
 * @property {any} widgetHosts
 * @property {string} welcomePageId
 */
class Config {

    constructor() {
        this.pageIds = [];
        this.widgetIds = [];
        this.pageHosts = {};
        this.widgetHosts = {};
        this.welcomePageId = undefined;
    }

    /**
     * @public
     * @param {string} id 
     * @returns {string}
     */
    getPageHost(id) {
        return this.pageHosts[id];
    }

    /**
     * @public
     * @param {string} id 
     * @returns {string} 
     */
    getWidgetHost(id) {
        return this.widgetHosts[id];
    }
}
