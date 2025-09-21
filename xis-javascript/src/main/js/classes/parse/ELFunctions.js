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