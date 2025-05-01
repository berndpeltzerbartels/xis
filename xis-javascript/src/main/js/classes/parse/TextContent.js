
/**
 * @typedef TextContent
 * @property {array} parts
 * Represents text containg static string parts
 * and variables like "My name is ${name}"
 */
class TextContent {

    constructor(parts) {
        this.parts = parts;
        this.registerClientStateVariables();
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
        return this.parts.map(part => part.asString(data)).reduce((s1, s2) => s1 + s2);
    }

    refreshOnClientStateChange() {
        this.evaluate(this.data);
    }

    registerClientStateVariables() {
        var variables = [];
        for (var part of this.parts) {
            if (part.expression && part.expression.type  == 'CLIENT_STATE_VARIABLE') {
                var key = part.expression.path.split('.')[0];
                app.clientState.registerListener(key,  () => this.refreshOnClientStateChange());
            }
        }
        return variables;
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
