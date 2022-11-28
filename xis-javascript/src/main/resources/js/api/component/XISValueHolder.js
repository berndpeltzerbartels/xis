class XISValueHolder extends XISTemplateObject {

    /**
     * @param {XISTemplateObject} parent (nullable)
     */
    constructor(parent) {
        super(parent);
        this.className = 'XISValueHolder';
        this.values = new XISMap();
    }

    /**
     * @public
     * @override
     */
    getValueHolder() {
        return this
    }

    /**
     * @public
     * @returns {XISMap}
     */
    getValues() {
        return this.values;
    }

    // TODO remove this method
    setVarnames(varNames) {
        throw new Error('remove this method');
    }

    addValues(values) {
        this.values.putAll(values);
    }

    /**
     * @param {Array<String>} path 
     */
    getValue(path) {
        var name = path[0];
        if (indexOf(this.values.keys()) != -1) {
            var rv = this.values.get(name);
            for (var i = 1; i < path.length; i++) {
                if (!rv) {
                    return undefined;
                }
                rv = rv[path[i]];
            }
            return rv;
        }
        if (!this.parentValueHolder) {
            throw new Error('no value for path ' + arrayToString(path));
        }
        return this.parentValueHolder.getValue(path);
    }


}