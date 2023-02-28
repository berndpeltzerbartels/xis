class ExpressionParser {

    constructor() {
        this.tokenizer = new Tokenizer();
        this.treeParser = new TreeParser();
    }
    parse(str) {
        var tokenArray = this.tokenizer.tokens(str);
        var root = new TokenLinker(tokenArray).linkTokens();
        return this.treeParser.parseTree(root);
    }

}

class Tokenizer {

    tokens(str) {
        var tokens = [];
        var buff = { numbers: false, alpha: false, chars: '', dot: false, negative: false };
        for (var i = 0; i < str.length; i++) {
            var c = str.charAt(i);
            switch (c) {
                case '\\':
                    if (i + 1 < str.length) {
                        buff.chars += str.charAt(++i);
                    } else {
                        buff.chars += '\\'
                    }
                    break;
                case '(':
                    this.endBuff(buff, tokens);
                    tokens.push({ type: 'OPEN_PAR' });
                    break;
                case ')':
                    this.endBuff(buff, tokens);
                    tokens.push({ type: 'CLOSE_PAR' });
                    break;
                case ',':
                    this.endBuff(buff, tokens);
                    tokens.push({ type: 'KOMMA' });
                    break;
                case '.':
                    this.endBuff(buff, tokens);
                    tokens.push({ type: 'DOT' });
                    break;
                case '&': {
                    if (buff.chars.length == 1 && buff.chars.charAt(0) == '&') {
                        tokens.push({ type: 'AND' });
                        this.resetBuffer(buff);
                    } else {
                        this.addTextChar(buff, c);
                    }
                    break;
                }
                case '|': {
                    if (buff.chars.length == 1 && buff.chars.charAt(0) == '|') {
                        tokens.push({ type: 'OR' });
                        this.resetBuffer(buff);
                    } else {
                        this.addTextChar(buff, c);
                    }
                    break;
                }
                case '\'':
                    if (buff.chars.length == 0 && buff.chars.charAt(0) == '\'') {
                        buff.chars += c;
                        this.endBuff(buff, tokens);
                    } else {
                        this.endBuff(buff, tokens);
                        buff.chars += c;
                    }
                    break;
                default: this.addTextChar(buff, c);

            }
        }
        this.endBuff(buff, tokens);
        return tokens;
    }

    resetBuffer(buff) {
        buff.numbers = false;
        buff.alpha = false;
        buff.dot = false;
        buff.negative = false;
        buff.chars = '';
    }


    endBuff(buff, tokens) {
        var rv;
        if (buff.chars != '') {
            if (buff.numbers) {
                if (buff.alpha) {
                    rv = this.addText(buff, tokens);
                } else {
                    rv = this.addNum(buff, tokens);
                }

            } else {
                rv = this.addText(buff, tokens);
            }
        }
        this.resetBuffer(buff);
        return rv;

    }

    addText(buff, tokens) {
        var token;
        if (buff.chars.charAt(buff.length - 1) == '\'' && buff.chars.charAt(0) == '\'') {
            token = { type: 'TEXT', value: buff.chars };
        } else {
            token = { type: 'NAME', value: trim(buff.chars) };
        }
        tokens.push(token);
        return token.type;
    }


    addNum(buff, tokens) {
        var token;
        if (buff.dot) {
            token = { type: 'DECIMAL', value: parseFloat(buff.chars) * buff.negative ? -1 : 1 };
        } else {
            token = { type: 'INT', value: parseInt(buff.chars) * buff.negative ? -1 : 1 };
        }
        tokens.push(token);
        return token.type;
    }

    addTextChar(buff, c) {
        if (c > -1 && c < 10) {
            buff.numbers = true;
            buff.chars += c;
        } else if (c == '.') {
            buff.dot = true;
            buff.chars += c;
        } else if (c == '-' && buff.chars.length == 0) {
            buff.negative = true;
        } else {
            buff.alpha = true;
            buff.chars += c;
        }
    }

}

class TokenLinker {

    constructor(tokens) {
        this.tokens = tokens;
    }

    linkTokens() {
        var root = this.tokens.shift();
        this.evaluate(root);
        return root;
    }

    evaluateParentheses(openPar) {
        var token = this.tokens.shift();
        while (token) {
            if (!openPar.subTree) {
                openPar.subTree = token;
            }
            if (token.type == 'OPEN_PAR') {
                token.next = this.evaluateParentheses(token);
                token = token.next;
            }

            var next = this.tokens.shift();
            if (next.type == 'CLOSE_PAR') {
                openPar.next = next;
                return next;
            }
            token.next = next;
            token = next;
        }
    }

    evaluate(token) {
        while (token) {
            if (token.type == 'OPEN_PAR') {
                token = this.evaluateParentheses(token);
            }
            var next = this.tokens.shift();
            token.next = next;
            token = next;
        }
    }

}

class TreeParser {

    parseTree(node) {
        if (this.isVar(node)) {
            return this.parseVar(node);
        } else if (this.isFunction(node)) {
            return this.parseFunction(node);
        } else if (this.isParenthesis(node)) {
            return this.parseParentheses(node);
        }
    }

    isFunction(node) {
        var type1 = node.type;
        var type2 = node.next ? node.next.type : undefined;
        return type1 == 'NAME' && type2 == 'OPEN_PAR'
    }

    isVar(node) {
        var type1 = node.type;
        var type2 = node.next ? node.next.type : undefined;
        return type1 == 'NAME' && type2 != 'OPEN_PAR'
    }

    isAnd(node) {
        return node.type == 'AND';
    }

    isOr(node) {
        return node.type == 'OR';
    }

    isLogocalOperator(node) {
        return this.isAnd(node) || this.isOr(node);
    }

    isParenthesis(node) {
        return node.type == 'OPEN_PAR';
    }

    parseFunction(node) {
        var name = node.value;
        var openPar = node.next;
        var paramterTree = this.parseParameters(openPar);
        if (!paramterTree) return false;
        return new Func(name, paramterTree);
    }

    parseVar(node) {
        var path = [];
        while (node) {
            var token0 = node;
            var token1 = node.next;
            var token2 = token1 ? token1.next : undefined;
            if (token0.type = 'NAME') {
                path.push(token0.value);
            }
            if (!token1) {
                break;
            }
            if (!token2) return false;
            if (token1.type == 'DOT' && token2.type == 'NAME') {
                path.push(token2.value);
            } else {
                return false;
            }
            node = token2.next;
        }
        return new Variable(path);
    }

    parseParentheses(node) {
        return new Paranethesis(node.subTree);
    }

    parseParameters(openPar) {
        if (!openPar.subTree) {
            return [];
        }
        var firstOfParam = openPar.subTree;
        var node = firstOfParam;
        var parameterSources = [node];
        while (node) {
            if (node.type == 'KOMMA') {
                node = node.next;
                parameterSources.push(node);
            }
            node = node.next;
        }
        var parameters = [];
        for (var first of parameterSources) {
            var param = this.parseParameter(first);
            if (!param) return false;
            parameters.push(param);
        }
        return parameters;
    }

    parseParameter(node) {
        var param;
        if (this.isVar(node)) {
            param = this.parseVar(node);
        } else if (this.isFunction(node)) {
            param = this.parseFunction(node);
        } else if (this.isParenthesis(node)) {
            param = this.parseParentheses(node);
        }
        if (node.next) {
            if (node.next.type == 'KOMMA') {
                return param;
            }
            if (this.isLogocalOperator(node.next)) {
                var operator = this.parseLogicalOperator(node.next);
                if (!operator) return param;
                param.next = operator;
            }
        }
        return param;
    }

    parseLogicalOperator(node) {
        if (!node.next) return false;
        var operator;
        if (this.isAnd(node)) {
            operator = new AndOperator();
        } else if (this.isOr()) {
            operator = new OrOperator();
        }
        if (!operator) return false;
        var next = this.parseParameter();
        if (!next) return false;
        operator.next = next;
        return operator;
    }

}

class Variable {

    constructor(path) {
        this.type = 'VAR';
        this.path = path;
        this.next = undefined;
    }

    evaluate(data) {
        var value = data.getValue(this.path);
        if (this.next) {
            var operator = this.next;
            var next = operator.next;
            if (operator.type == 'AND') {
                return value && next.evaluate(data);
            } else if (operator.type == 'OR') {
                return value || next.evaluate(data);
            }
            throw new Error('operator expected');
        }
        return value;
    }
}

class Paranethesis {

    constructor(firstChild) {
        this.firstChild = firstChild;
        this.next = undefined;
        this.type = 'PARENTHESES';
    }

    evaluate(data) {
        var value = this.firstChild.evaluate(data);
        if (this.next) {
            var operator = this.next;
            var next = operator.next;
            if (operator.type == 'AND') {
                return value && next.evaluate(data);
            } else if (operator.type == 'OR') {
                return value || next.evaluate(data);
            }
            throw new Error('operator expected');
        }
        return value;
    }
}

class Func {

    constructor(name, parameters) {
        this.name = name;
        this.parameters = parameters;
        this.next = undefined;
        this.type = 'FUNCTION';
    }

    evaluate(data) {
        var paramValues = this.parameters.map(p => p.evaluate(data));
        var value = window[this.name].apply(null, paramValues);
        if (this.next) {
            var operator = this.next;
            var next = operator.next;
            if (operator.type == 'AND') {
                return value && next.evaluate(data);
            } else if (operator.type == 'OR') {
                return value || next.evaluate(data);
            }
            throw new Error('operator expected');
        }
        return value;
    }
}

class AndOperator {
    constructor() {
        this.type == 'AND';
    }
}

class OrOperator {
    constructor() {
        this.type == 'OR';
    }
}

class Noop {
    evaluate(data) { }
}

/**
 * 
 * @param {string} str 
 * @returns 
 */
function trim(str) {
    var start = 0;
    for (; start < str.length; start++) {
        if (!isWhitespace(str.charAt(start))) {
            break
        }
    }
    var end = str.length - 1;
    for (; end >= start; end--) {
        if (!isWhitespace(str.charAt(end))) {
            break
        }
    }
    return str.substring(start, end + 1);

}


function isWhitespace(c) {
    return c == ' ' || c == '\t' || c == '\n' || c == '\r';
}

function cloneArr(arr) {
    return arr.map(v => v);
}