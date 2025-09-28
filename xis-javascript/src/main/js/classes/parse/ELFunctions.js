class ELFunctions {

    constructor() {
        this.functions = {};
        this.functions['length'] = arg => this.length(arg);
        this.functions['size'] = arg => this.length(arg);
        this.functions['count'] = arg => this.length(arg);
        this.functions['toUpperCase'] = arg => this.toUpperCase(arg);
        this.functions['toLowerCase'] = arg => this.toLowerCase(arg);
        this.functions['empty'] = arg => this.empty(arg);
        this.notEmpty = arg => !this.empty(arg);

        // Date/Time formatter functions
        this.functions['formatDate'] = (date, locale) => this.formatDate(date, locale);
        this.functions['formatDateTime'] = (date, locale) => this.formatDateTime(date, locale);
        this.functions['formatTime'] = (date, locale) => this.formatTime(date, locale);

        // String/Array utilities
        this.functions['join'] = (arr, sep) => this.join(arr, sep);
        this.functions['split'] = (str, sep) => this.split(str, sep);
        this.functions['substring'] = (str, start, end) => this.substring(str, start, end);
        this.functions['replace'] = (str, search, replaceVal) => this.replace(str, search, replaceVal);
        this.functions['trim'] = str => this.trim(str);

        // Number/Math
        this.functions['round'] = (val, digits) => this.round(val, digits);
        this.functions['floor'] = val => this.floor(val);
        this.functions['ceil'] = val => this.ceil(val);
        this.functions['abs'] = val => this.abs(val);

        // Logic
        this.functions['default'] = (val, fallback) => this.defaultValue(val, fallback);
        this.functions['contains'] = (container, value) => this.contains(container, value);

        // Object/Array
        this.functions['keys'] = obj => this.keys(obj);
        this.functions['values'] = obj => this.values(obj);
        this.functions['hasKey'] = (obj, key) => this.hasKey(obj, key);

        // Date/Time extractors
        this.functions['year'] = date => this.year(date);
        this.functions['month'] = date => this.month(date);
        this.functions['day'] = date => this.day(date);
        this.functions['hour'] = date => this.hour(date);
        this.functions['minute'] = date => this.minute(date);
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
        if (str == null) return null;
        if (typeof str !== 'string') return str;
        return str.toUpperCase();
    }

    toLowerCase(str) {
        if (str == null) return null;
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

}

const elFunctions = new ELFunctions();