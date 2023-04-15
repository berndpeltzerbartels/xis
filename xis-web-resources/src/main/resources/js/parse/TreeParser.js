
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
        return new Paranetheses(node.subTree);
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

class Paranetheses {

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
        var fu = window[this.name];
        if (!fu) throw new Error('no such function: ' + this.name);
        var value = fu.apply(null, paramValues);
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

