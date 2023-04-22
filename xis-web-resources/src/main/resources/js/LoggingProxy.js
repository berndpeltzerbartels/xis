class LoggingProxy {

    constructor(obj) {
        this.obj = obj;
        for (var method of this.methods()) {
            this[method.name] = function () {
                console.log(method.name + '(' + this.formatParameters(arguments) + ")");
                return this.obj[method.name].apply(this, arguments);
            }
        }
    }

    methods() {
        var methods = [];
        for (var name of Object.getOwnPropertyNames(Object.getPrototypeOf(this.obj))) {
            var prop = this.obj[name];
            if (typeof prop == 'function') {
                methods.push(prop);
            }
        }
        return methods;
    }

    formatParameters(arr) {
        var result = [];
        for (const val of arr) {
            result.push(this.formatValue(val));
        }
        return result.join(',');
    }

    formatArray(arr) {
        return "[" + this.formatParameters(arr) + "]";
    }

    formatValue(o) {
        if (!isNaN(o)) {
            return o;
        }
        if (o == undefined) {
            return undefined;
        }
        if (typeof o == 'string') {
            return o;
        }
        if (typeof o == 'array') {
            return this.formatArray(o);
        }
        if (typeof o == 'object') {
            return JSON.stringify(o);
        }
        return o;
    }

}