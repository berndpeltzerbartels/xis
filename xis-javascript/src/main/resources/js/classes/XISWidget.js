class XISWidget {


    constructor(data) {

    }

    createChildren() {
        
    }

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
        return this.valueHolder.getValue(path);
    }


}