class XISValueHolder extends XISTemplateObject {

    /**
     * @param {XISValueHolder} parentValueHolder
     */
    constructor(parentValueHolder) {
        this.parentValueHolder = parentValueHolder;
        this.varNames = varNames;
        this.values = {};
    }
    
    setVarnames(varNames) {
        this.varNames = varNames;
    }
    /**
     * @param {Array<String>} path 
     */
     getValue(path) {
        var name = path[0];
        if (this.varNames.indexOf(name) != -1) {
            var rv = this.values[name];
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