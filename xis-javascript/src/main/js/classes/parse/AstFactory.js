class AstFactory {
    constructor(tokens, functions) {
        this.tokens = tokens;
        this.functions = functions;
        this.index = 0;
    }

    createAst() {
        if (this.tokens.length === 0) {
            return new NoopAst();
        }
        return this.parse();
    }

    parse() {
        var row = [];
        while (this.index < this.tokens.length) {
            const token = this.tokens[this.index];
            switch (this.currentToken().type) {
                case OPEN_BRACKET:
                    this.consumeToken(OPEN_BRACKET);
                    row.push(this.parse());
                    break;
                case CLOSE_BRACKET:
                    return this.toExpression(row);
                case IDENTIFIER:
                    row.push(this.createVariable(this.consumeToken(IDENTIFIER)));
                    break;
                case FLOAT:
                case INTEGER:
                case STRING:
                case BOOL:
                case NULL_OR_UNDEFINED:
                    row.push(this.createConstant(this.consumeToken()));
                    break;
                case ARRAY_START:
                    return this.parseArray();
                case COMMA:
                    break;
                case AND:
                case OR:
                case NOT: // TODO
                case EQUAL:
                case NOT_EQUAL:
                case GREATER:
                case GREATER_EQUAL:
                case LESS:
                case LESS_EQUAL:
                case ADD:
                case SUB:
                case MUL:
                case DIV:
                case MOD:
                case ASSIGN:
                case ADD_ASSIGN:
                case SUB_ASSIGN:
                case MUL_ASSIGN:
                case DIV_ASSIGN:
                case MOD_ASSIGN:
                case INCREMENT:
                    row.push(this.createOperator(this.consumeToken()));
                    break;
                default:
                    throw new Error("Unexpected token: " + this.currentToken().type);
            }
        }
        return this.toExpression(row);
    }

    toExpression(row) {
        switch (row.length) {
            case 0: return [];
            case 1: return row[0];
            default:
                var result;
                for (var precedence = 3; precedence >= 0; precedence--) {
                    const operator  = this.expressionForPrecedence(row, precedence);
                    if (operator) {
                        result = operator;
                    }
                }
                return result;
        }
    }

    expressionForPrecedence(row, precedence) {
        var i = 0;
        var operator;
        while (i < row.length) {
            var element = row[i];
            if (this.isOperator(element) && element.precedence == precedence) {
                const left = row[i - 1];
                const right = row[i + 1];
                operator = row[i];
                operator.left = left;
                operator.right = right;
                this.replace(row, i - 1, 3, operator);
            } else {
                i++;
            }
        }
        return operator;
    }

    replace(arr, startIndex, count, replacement) {
        arr.splice(startIndex + 1, count - 1);
        arr[startIndex] = replacement;
    }

    nextToken() {
        return this.tokens[this.index + 1];
    }

    consumeToken(type) {
        const token = this.currentToken();
        if (type && token.type !== type) {
            throw new Error("Expected token of type " + type + " but got " + token.type);
        }
        this.index++;
        return token;
    }

    currentToken() {
        return this.tokens[this.index];
    }

    parseArray() {
        const array = [];
        this.consumeToken(ARRAY_START);
        while (this.currentToken().type !== ARRAY_END) {
            if (this.currentToken().type === COMMA) {
                this.consumeToken(COMMA);
            } else {
                array.push(this.parse());
            }
        }
        this.consumeToken(ARRAY_END);
        return array;
    }

    parseParameters() {
        const array = [];
        this.consumeToken(OPEN_BRACKET);
        while (this.currentToken().type !== CLOSE_BRACKET) {
            if (this.currentToken().type === COMMA) {
                this.consumeToken(COMMA);
            } else {
                array.push(this.parse());
            }
        }
        this.consumeToken(CLOSE_BRACKET);
        return array;
    }


    parseFunctionCall() {
        const functionName = this.consumeToken(IDENTIFIER);
        const fct = this.functions[functionName.value];
        if (fct === undefined) {
            throw new Error(`Function ${functionName.value} not found`);
        }
        this.consumeToken(OPEN_BRACKET);
        const parameters = [];
        while (this.currentToken().type !== CLOSE_BRACKET) {
            parameters.push(this.parse());
            if (this.currentToken().type === COMMA) {
                this.consumeToken(COMMA);
            }
        }
        this.consumeToken(CLOSE_BRACKET);
        return new FunctionCall(fct, parameters);
    }


    isOperator(token) {
        switch (token.type) {
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MOD:
            case AND:
            case OR:
            case EQUAL:
            case NOT_EQUAL:
            case GREATER:
            case LESS:
            case GREATER_EQUAL:
            case LESS_EQUAL:
                return true;
            default:
                return false;
        }
    }

    isConstant(token) {
        switch (token.type) {
            case NUMBER:
            case STRING:
            case BOOLEAN:
                return true;
            default:
                return false;
        }
    }

    createVariable(token) {
        return new Variable2(token.value);
    }

    createConstant(token) {
        return new Constant(token.value);
    }


    createOperator(token) {
        return new Operator(token);
    }

}

class Operator {

    constructor(token) {
        this.type = token.type;
        this.left = undefined;
        this.right = undefined;
        this.binaryFunction = this.operatorFunction(token);
        this.precedence = this.getPrecedence(token.type);
    }

    evaluate(data) {
        const leftValue = this.left.evaluate(data);
        const rightValue = this.right.evaluate(data);
        return this.binaryFunction(leftValue, rightValue);
    }

    operatorFunction(token) {
        switch (token.type) {
            case ADD: return (a, b) => a + b;
            case SUB: return (a, b) => a - b;
            case MUL: return (a, b) => a * b;
            case DIV: return (a, b) => a / b;
            case MOD: return (a, b) => a % b;
            case AND: return (a, b) => a && b;
            case OR: return (a, b) => a || b;
            case EQUAL: return (a, b) => a === b;
            case NOT_EQUAL: return (a, b) => a !== b;
            case GREATER: return (a, b) => a > b;
            case LESS: return (a, b) => a < b;
            case GREATER_EQUAL: return (a, b) => a >= b;
            case LESS_EQUAL: return (a, b) => a <= b;
            default:
                throw new Error("Unknown operator: " + token.type);
        }
    }

    // Liefert einen numerischen Wert für die jeweilige Präzedenz.
    getPrecedence(tokenType) {
        debugger;
        // Höherer Rückgabewert = höhere Präzedenz
        switch (tokenType) {
            case MUL:
            case DIV:
            case MOD:
                return 2;
            case ADD:
            case SUB:
                return 1;
            // Weitere Operatoren wie AND, OR, Vergleichsoperatoren erhalten hier ggf. niedrigere Werte.
            case AND:
            case OR:
            case EQUAL:
            case NOT_EQUAL:
            case GREATER:
            case LESS:
            case GREATER_EQUAL:
            case LESS_EQUAL:
                return 0;
            default:
                return -1;
        }
    }
}

class FunctionCall {
    constructor(name, parameters) {
        this.type = 'FUNCTION_CALL';
        this.name = name;
        this.parameters = parameters;
    }

    evaluate(data) {
        return this.name(this.parameters.map(p => p.evaluate(data)));
    }
}

class Constant {
    constructor(value) {
        this.type = 'CONSTANT';
        this.value = value;
    }

    evaluate(data) {
        return this.value;
    }
}

class Variable2 {
    constructor(path) {
        this.type = 'VARIABLE';
        this.path = path;
    }

    evaluate(data) {
        return data.getValueByPath(this.path);
    }
}

class NoopAst {
    constructor() {
        this.type = 'NOOP';
    }

    evaluate(data) {
        return '';
    }
}
