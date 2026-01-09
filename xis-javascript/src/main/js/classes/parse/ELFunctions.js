class ELFunctions {

    constructor() {
        this.functions = {};
        this.functions['length'] = this.length.bind(this);
        this.functions['size'] = this.length.bind(this);
        this.functions['count'] = this.length.bind(this);
        this.functions['toUpperCase'] = this.toUpperCase.bind(this);
        this.functions['toLowerCase'] = this.toLowerCase.bind(this);
        this.functions['empty'] = this.empty.bind(this);
        this.functions['isEmpty'] = this.empty.bind(this);
        this.functions['notEmpty'] = this.notEmpty.bind(this);

        // Date/Time formatter functions
        this.functions['formatDate'] = this.formatDate.bind(this);
        this.functions['formatDateTime'] = this.formatDateTime.bind(this);
        this.functions['formatTime'] = this.formatTime.bind(this);

        // String/Array utilities
        this.functions['join'] = this.join.bind(this);
        this.functions['split'] = this.split.bind(this);
        this.functions['substring'] = this.substring.bind(this);
        this.functions['replace'] = this.replace.bind(this);
        this.functions['trim'] = this.trim.bind(this);

        // Number/Math
        this.functions['round'] = this.round.bind(this);
        this.functions['floor'] = this.floor.bind(this);
        this.functions['ceil'] = this.ceil.bind(this);
        this.functions['abs'] = this.abs.bind(this);
        this.functions['sum'] = this.sum.bind(this);

        // Logic
        this.functions['default'] = this.defaultValue.bind(this);
        this.functions['defaultValue'] = this.defaultValue.bind(this);
        this.functions['contains'] = this.contains.bind(this);
        this.functions['flatMap'] = this.flatMap.bind(this);
        this.functions['filter'] = this.filter.bind(this);

        // Object/Array
        this.functions['keys'] = this.keys.bind(this);
        this.functions['values'] = this.values.bind(this);
        this.functions['hasKey'] = this.hasKey.bind(this);

        // Date/Time extractors
        this.functions['year'] = this.year.bind(this);
        this.functions['month'] = this.month.bind(this);
        this.functions['day'] = this.day.bind(this);
        this.functions['hour'] = this.hour.bind(this);
        this.functions['minute'] = this.minute.bind(this);
    }

    // String/Array utilities
    join(arr, sep) {
        if (!Array.isArray(arr)) return '';
        return arr.join(sep == null ? ',' : sep);
    }
    split(str, sep) {
        if (typeof str !== 'string') return [];
        return str.split(sep == null ? ',' : sep);
    }
    substring(str, start, end) {
        if (typeof str !== 'string') return str;
        return str.substring(start, end);
    }
    replace(str, search, replaceVal) {
        if (typeof str !== 'string') return str;
        return str.replace(search, replaceVal);
    }
    trim(str) {
        if (typeof str !== 'string') return str;
        return str.trim();
    }

    // Number/Math
    round(val, digits) {
        if (typeof val !== 'number') val = Number(val);
        if (isNaN(val)) return 0;
        if (digits == null) return Math.round(val);
        const factor = Math.pow(10, digits);
        return Math.round(val * factor) / factor;
    }
    floor(val) {
        if (typeof val !== 'number') val = Number(val);
        if (isNaN(val)) return 0;
        return Math.floor(val);
    }
    ceil(val) {
        if (typeof val !== 'number') val = Number(val);
        if (isNaN(val)) return 0;
        return Math.ceil(val);
    }
    abs(val) {
        if (typeof val !== 'number') val = Number(val);
        if (isNaN(val)) return 0;
        return Math.abs(val);
    }

    sum(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19) {
        if (!arguments || arguments.length === 0) return 0;
        let total = 0;
        for (let i = 0; i < arguments.length; i++) {
            const value = arguments[i];
            if (!value) continue;
            if (Array.isArray(value)) {
                for (const item of value) {
                    if (isFloat(item)) {
                        total += item;
                        continue;
                    }
                    if (isInt(item)) {
                        total += item;
                        continue;
                    }
                    const integerValue = parseInt(item, 10);
                    if (!isNaN(integerValue)) {
                        total += integerValue;
                        continue;
                    }
                    const floatValue = parseFloat(item);
                    if (!isNaN(floatValue)) {
                        total += floatValue;
                        continue;
                    }
                }

            } else if (isFloat(value)) {
                return total += value;
            } else if (isInt(value)) {
                total += value;
            } else {
                const integerValue = parseInt(value, 10);
                if (!isNaN(integerValue)) {
                    total += integerValue;
                }
                const floatValue = parseFloat(value);
                if (!isNaN(floatValue)) {
                    total += floatValue;
                }
            }

        }
        return total;
    }

    flatMap(value, path) {
        if (!Array.isArray(value)) {
            value = [value];
        }
        
        const pathParts = doSplit(path, '.');
        let results = value;

        // Navigate through each path segment
        for (const pathPart of pathParts) {
            const nextResults = [];

            for (const item of results) {
                if (item == null) continue;

                const data = new Data(item);
                const val = data.getValue([pathPart]);

                // If value is array, flatten it
                if (Array.isArray(val)) {
                    nextResults.push(...val);
                } else if (val != null) {
                    nextResults.push(val);
                }
            }

            results = nextResults;
        }
        
        return results;
    }

    // Logic
    defaultValue(val, fallback) {
        return (val == null || val === '' || (Array.isArray(val) && val.length === 0)) ? fallback : val;
    }
    contains(container, value) {
        if (typeof container === 'string') return container.includes(value);
        if (Array.isArray(container)) return container.includes(value);
        if (container && typeof container === 'object') return Object.values(container).includes(value);
        return false;
    }

    // Object/Array
    keys(obj) {
        if (obj == null || typeof obj !== 'object') return [];
        return Object.keys(obj);
    }
    values(obj) {
        if (obj == null || typeof obj !== 'object') return [];
        return Object.values(obj);
    }
    hasKey(obj, key) {
        if (obj == null || typeof obj !== 'object') return false;
        return Object.prototype.hasOwnProperty.call(obj, key);
    }

    // Date/Time extractors
    year(date) {
        let d = this.parseDate(date);
        return d ? d.getFullYear() : null;
    }
    month(date) {
        let d = this.parseDate(date);
        return d ? d.getMonth() + 1 : null;
    }
    day(date) {
        let d = this.parseDate(date);
        return d ? d.getDate() : null;
    }
    hour(date) {
        let d = this.parseDate(date);
        return d ? d.getHours() : null;
    }
    minute(date) {
        let d = this.parseDate(date);
        return d ? d.getMinutes() : null;
    }

    // Helper to parse input to Date object
    parseDate(date) {
        if (date instanceof Date) return date;
        if (typeof date === 'number') return new Date(date);
        if (typeof date === 'string') {
            // Accept ISO, yyyy-MM-dd, yyyy-MM-ddTHH:mm:ss, etc.
            let d = new Date(date);
            if (!isNaN(d.getTime())) return d;
        }
        return null;
    }

    formatDate(date, locale) {
        let d = this.parseDate(date);
        if (!d) return '';
        try {
            return new Intl.DateTimeFormat(locale || undefined, { dateStyle: 'medium' }).format(d);
        } catch (e) {
            return d.toLocaleDateString(locale || undefined);
        }
    }

    formatDateTime(date, locale) {
        let d = this.parseDate(date);
        if (!d) return '';
        try {
            return new Intl.DateTimeFormat(locale || undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(d);
        } catch (e) {
            return d.toLocaleString(locale || undefined);
        }
    }

    formatTime(date, locale) {
        let d = this.parseDate(date);
        if (!d) return '';
        try {
            return new Intl.DateTimeFormat(locale || undefined, { timeStyle: 'short' }).format(d);
        } catch (e) {
            return d.toLocaleTimeString(locale || undefined);
        }
    }


    addFunction(name, func) {
        this.functions[name] = func;
    }

    length(arg) {
        if (arg == null) return 0;
        if (typeof arg === 'string') {
            return arg.length;
        }
        if (Array.isArray(arg)) {
            return arg.length;
        }
        if (arg instanceof Map) {
            return arg.size;
        }
        if (arg instanceof Set) {
            return arg.size;
        }
        if (typeof arg === 'object') {
            return Object.keys(arg).length;
        }
        return 0;
    }

    toUpperCase(str) {
        if (str == null) return '';
        if (typeof str !== 'string') return str;
        return str.toUpperCase();
    }

    toLowerCase(str) {
        if (str == null) return '';
        if (typeof str !== 'string') return str;
        return str.toLowerCase();
    }

    empty(arg) {
        if (arg == null) return true;
        if (typeof arg === 'string') {
            return arg.length === 0;
        }
        if (Array.isArray(arg)) {
            return arg.length === 0;
        }
        if (arg instanceof Map) {
            return arg.size === 0;
        }
        if (arg instanceof Set) {
            return arg.size === 0;
        }
        return false;
    }

    notEmpty(arg) {
        if (arg == null) return false;
        if (typeof arg === 'string') {
            return arg.length > 0;
        }
        if (Array.isArray(arg)) {
            return arg.length > 0;
        }
        if (arg instanceof Map) {
            return arg.size > 0;
        }
        if (arg instanceof Set) {
            return arg.size > 0;
        }
        return false;
    }

    // Filter array by property value
    filter(arr, property, value) {
        if (!Array.isArray(arr)) return [];
        return arr.filter(item => {
            if (item == null) return false;
            const data = new Data(item);
            const pathParts = doSplit(property, '.');
            const propValue = data.getValue(pathParts);
            return propValue === value;
        });
    }

}

const elFunctions = new ELFunctions();