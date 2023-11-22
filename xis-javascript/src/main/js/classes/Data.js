/**
 * Hierarchical page-data
 */
class Data {

    /**
     *
     * @param {any} values
     * @param {Data} parentData
     */
    constructor(values, parentData = undefined) {
        this.values = values;
        this.parentData = parentData;
    }
    /**
     * @public
     * @param {Array<string>} path the path of the data value
     * @returns {any}
     */
    getValue(path) {
        var dataNode = this.values;
        for (var i = 0; i < path.length; i++) {
            var key = path[i];
            if (dataNode[key]) {
                dataNode = dataNode[key];
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

    /**
     * @public
     * @returns {Array<string>}
     */
    getKeys() {
        return Object.keys(this.values);
    }

    /**
     * @public
     * @param {String} key
     * @param {any} value
     */
    setValue(path, value) {
        var parentDataNode = this.values;
        for (var i = 0; i < path.length - 1; i++) {
            var key = path[i];
            var dataNode = parentDataNode[key];
            if (!dataNode) {
                dataNode = {};
                parentDataNode[key] = dataNode;
            }
            parentDataNode = dataNode;
        }
        var key = path[path.length - 1];
        parentDataNode[key] = value;
    }


    /**
     @ @public
     * @param {String[]} keys 
     * @return {any}
     */
    getValues(keys) {
        var result = {};
        for (var key of keys) {
            result[key] = this.values[key];
        }
        return result;
    }
}

