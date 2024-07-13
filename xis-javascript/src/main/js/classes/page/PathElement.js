/**
 * Path-varaible or static string. 
 * One of it's attributes (content, key) is null.
 *
 * @property {string} content
 * @property {string} key
 * @property {PathElement} next
 * @property {string} type
 */

class PathElement {

    // TODO create a typed converter for response json
    /**
     * 
     * @param {any} obj json result 
     */
    constructor(obj) {
        this.content = obj.content;
        this.key = obj.key;
        this.type = obj.type;
        if (obj && obj.next) {
            this.next = new PathElement(obj.next);
        }
    }

    /**
     * @public
     * @param {string} path 
     * @param {string: string} pathVariables resulting variables are getting added to this parameter 
     */
    evaluate(path, pathVariables) {
        if (this.type == 'variable') {
            return this.evaluateVar(path, pathVariables);
        } else if (this.type == 'static') {
            return this.evaluateStatic(path, pathVariables);
        } else {
            throw new Error('unknown fragment-type:' + this.type);
        }
    }

    /**
    * @private
    * @param {string} path 
    * @param {Array<string: string>} pathVariables resulting variables are getting added to this parameter
    * @returns {boolean}
    */
    evaluateVar(path, pathVariables) {
        if (this.next) {
            var nextStaticStr = this.next.content;
            var pos = path.indexOf(nextStaticStr);
            if (pos == -1) {
                return false;
            }
            var key = this.key;
            var value = path.substring(0, pos);
            var item = {};
            item[key] = value;
            pathVariables.push(item);
            if (this.next) {
                return this.next.evaluateStatic(path.substring(pos), pathVariables);
            }
            return true;
        }
    }

    /**
    * @private
    * @param {string} path 
    * @param {string: string} pathVariables resulting variables are getting added to this parameter
    * @returns {boolean} 
    */
    evaluateStatic(path, pathVariables) {
        if (!path.startsWith(this.content)) {
            return false;
        }
        if (this.next) {
            return this.next.evaluateVar(path.substring(this.content.length), pathVariables);
        }
        return true;
    }

    /**
     * Writes the represenation of each 
     * part of the path into a result-array.
     * Path variables are represented as an asterisk.
     * 
     * @returns {string} result 
     */
    appendNormalized() {
        var str = this.type == 'static' ? this.content : '*';
        if (this.next) {
            str += this.next.appendNormalized();
        }
        return str;
    }
}