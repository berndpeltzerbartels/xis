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
        this.validationPath = '';
        this.scope = 'default';
    }
    /**
     * @public
     * @param {Array<string>} path the path of the data value
     * @returns {any}
     */
    getValue(path) {
        if (typeof path === 'string') {
            path = doSplit(path, '.');
        }
        var dataNode = this.values;
        for (var i = 0; i < path.length; i++) {
            var key = path[i];
            if (dataNode[key] != undefined) {  // false failes for 0
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

    getValueByPath(pathStr) {
        return this.getValue(doSplit(pathStr, '.'));
    }

    /**
     * @public
     * @returns {Array<string>}
     */
    getKeys() {
        return Object.keys(this.values);
    }

    setValueByPath(pathStr, value) {
        this.setValue(doSplit(pathStr, '.'), value);
    }
    /**
     * @public
     * @param {array<string>} path
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
        if (parentDataNode[key]) {
            if (!Array.isArray(parentDataNode[key])) {
                parentDataNode[key] = [parentDataNode[key]];
            }
            parentDataNode[key].push(value);
        } else {
            parentDataNode[key] = value;
        }

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

