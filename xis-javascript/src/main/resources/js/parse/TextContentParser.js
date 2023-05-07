class TextContentParser {

    constructor(src) {
        this.chars = new CharIterator(src);
        this.parts = [];
    }

    parse() {
        this.readText();
        return new TextContent(this.parts);
    }


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

    createTextPart(text) {
        return {
            text: text,
            asString: function (data) {
                return this.text;
            }
        };
    }

    tryCreateVarPart(src) {
        var expression = new ExpressionParser().parse(src);
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