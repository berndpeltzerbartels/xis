const SCOPE_TREE = 'tree';
const SCOPE_CONTROLLER = 'controller';

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
        this.scope = SCOPE_TREE;
    }
    /**
     * @public
     * @param {Array<string>} path the path of the data value
     * @returns {any}
     */
    getValue(path) {
        if (path == undefined || path.length == 0) {
            throw new Error('Path is undefined or empty (1)');
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
        return this.getValue(this.doSplit(pathStr));
    }

    /**
     * @public
     * @returns {Array<string>}
     */
    getKeys() {
        return Object.keys(this.values);
    }

    setValueByPath(pathStr, value) {
        if (path == undefined || path.length == 0) {
            throw new Error('Path is undefined or empty (2)');
        } 
        this.setValue(this.doSplit(pathStr, '.'), value);
    }
    /**
     * @public
     * @param {array<string>} path
     * @param {any} value
     */
    setValue(path, value) {
        if (path == undefined || path.length == 0) {
            throw new Error('Path is undefined or empty (3)');
        } 
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

    /**
 * Splits a path like "a[2].b.c[3]" into parts: ["a", "2", "b", "c", "3"]
 * Ultra-fast: No regex
 * @param {string} path 
 * @param {string} separator 
 * @returns {Array<string>}
 */
 doSplit(path, separator = '.') {
    if (path == undefined || path.length == 0) {
        throw new Error('Path is undefined or empty (4)');
    }
    var parts = [];
    var current = '';
    for (var i = 0; i < path.length; i++) {
        var ch = path[i];
        if (ch === separator) {
            if (current.length > 0) {
                parts.push(current);
                current = '';
            }
        } else if (ch === '[') {
            if (current.length > 0) {
                parts.push(current);
                current = '';
            }
            i++;
            var idx = '';
            while (i < path.length && path[i] !== ']') {
                idx += path[i];
                i++;
            }
            if (idx.length > 0) {
                parts.push(idx);
            }
        } else {
            current += ch;
        }
    }
    if (current.length > 0) {
        parts.push(current);
    }
    return parts;
}
}

