
/**
 * @typedef TextContent
 * @property {array} parts
 * Represents text containg static string parts
 * and variables like "My name is ${name}"
 */
class TextContent {

    /**
     * @param {array} parts
     */
    constructor(parts) {
        this.parts = parts;
    }

    /**
     * @public
     * @param {Data} data
     * @returns the string we get after replacing the
     * variables with the actual data.
     */
    evaluate(data) {
        this.data = data;
        if (this.parts.length == 0) return '';
        
        let result = '';
        for (let part of this.parts) {
            var partResult = part.asString(data);
            if (partResult === undefined || partResult === null) {
               continue;
            }
            result += partResult;
        }
        return result;
    }

    /**
     * Refresh the text content. This is used when store data (sessionStorage or localStorage)
     * is changed and we need to refresh the text content. The date from controller of the 
     * embedding component is re-used to refresh the text content, while the new stored 
     * data is loaded during evaluation.
     * 
     * @public
     */
    doRefresh() {
        if (this.data) { // text content inside invoker is not evaluated yet and so data is not set
            this.evaluate(this.data);
        }
    }
 
    clone() {
        var parts = [];
        for (var part of this.parts) {
            if (part.expression) {
                parts.push({
                    expression: part.expression,
                    asString: function (data) {
                        return this.expression.evaluate(data);
                    }
                });
            } else {
                parts.push({
                    text: part.text,
                    asString: function (data) {
                        return this.text;
                    }
                });
            }
        }
    return new TextContent(parts);
    }

}
