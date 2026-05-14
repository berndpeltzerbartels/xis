/**
* Parses text content with embedded expressions in the form of ${expression}.
* Example: "Hello, ${user.name}! You have ${user.notifications.length} new notifications."
*/
class TextContentParser {

    /**
    * Parses text content with embedded expressions in the form of ${expression}.
    * @param {string} src - The source text to parse.
    * @returns {TextContent} The parsed TextContent object.
    */
    constructor(src) {
        this.chars = new CharIterator(src);
        this.parts = [];
    }

    /**
    * @public
    * Parses the source text and returns a TextContent object.
    * @returns {TextContent} The parsed TextContent object.
    */
    parse() {
        this.readText();
        return new TextContent(this.parts);
    }


    /**
    * @private
    * Reads text until an expression is found and processes it.
    * @returns {string}
    */
    readText() {
        var buff = '';
        while (this.chars.hasNext()) {
            var currentChar = this.chars.next();
            if (currentChar == '$' && this.chars.afterCurrent() == '{') {
                if (buff.length > 0) {
                    this.parts.push(this.createTextPart(buff));
                }
                this.chars.next();
                this.readVar();
                buff = '';
                continue;
            }
            buff += currentChar;
        }
        if (buff.length > 0) {
            this.parts.push(this.createTextPart(buff));
        }

    }

    /**
    * @private
    * Reads an expression until the closing brace is found and processes it.
    */
    readVar() {
        var buff = '';
        while (this.chars.hasNext()) {
            var currentChar = this.chars.next();
            if (currentChar == '}' && this.chars.beforeCurrent() != '\\') {
                if (buff.length > 0) {
                    var varPart = this.tryCreateVarPart(buff);
                    if (varPart) {
                        this.parts.push(varPart);
                    } else {
                        this.parts.push(this.createTextPart(buff));
                    }
                }
                return;
            }
            buff += currentChar;
        }
    }

    /**
    * @private
    * Creates a text part object.
    * @param {string} text - The text content.
    * @returns {TextPart} The text part object.
    */
    createTextPart(text) {
        return {
            text: text,
            asString: function (data) {
                return this.text;
            }
        };
    }

    /**
    * @private
    * Tries to create a variable part object by parsing the expression.
    * @param {string} src - The expression source.
    * @returns {VarPart|false} The variable part object or false if parsing failed.
    */
    tryCreateVarPart(src) {
    var expression = new ExpressionParser(elFunctions).parse(src);
        if (expression) {
            return {
                expression: expression,
                asString: function (data) {
                    return this.expression.evaluate(data);
                }
            };
        }
        return false;
    }
}