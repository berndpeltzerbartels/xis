class AstFactory {
    constructor(tokens, functions, originalExpression) {
        this.tokens = tokens;
        this.functions = functions;
        this.originalExpression = originalExpression;
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
            console.log(row);
            const token = this.tokens[this.index];
            switch (this.currentToken().type) {
                case OPEN_BRACKET:
                    this.consumeToken(OPEN_BRACKET);
                    row.push(this.parse());
                    this.consumeToken(CLOSE_BRACKET);
                    break;
                case CLOSE_BRACKET:
                case CLOSING_SQUARE_BRACKET:
                    return this.toExpression(row);
                case IDENTIFIER:
                    if (this.nextToken().type === OPEN_BRACKET) {
                        row.push(this.parseFunctionCall());
                    } else if (this.nextToken().type === OPENING_SQUARE_BRACKET) {
                        row.push(this.createPropertyVariable());
                    } else {
                        row.push(this.createVariable(this.consumeToken(IDENTIFIER)));

                    }
                    break;
                case FLOAT:
                case INTEGER:
                case STRING:
                case BOOL:
                case NULL_OR_UNDEFINED:
                    row.push(this.createConstant(this.consumeToken()));
                    break;
                case OPENING_SQUARE_BRACKET:
                    return this.parseArray();
                case COMMA:
                    return this.toExpression(row);
                case SUB:
                    debugger;
                    if ((row.length === 0 || this.isOperator(row[row.length - 1]))
                        && ([FLOAT, INTEGER].indexOf(this.nextToken().type) !== -1)) {
                        this.consumeToken(SUB);
                        row.push(this.createNegativeConstant(this.consumeToken()));
                    } else {
                        row.push(this.createOperator(this.consumeToken()));
                    }
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
                    throw new Error("Unexpected token in '" + this.originalExpression + "' : " + this.currentToken().type);
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
                    const operator = this.expressionForPrecedence(row, precedence);
                    if (operator) {
                        result = operator;
                    }
                }
                return result;
        }
    }

    expressionForPrecedence(row, precedence) {
        var i = 1;
        var operator;
        while (i < row.length) {
            var element = row[i];
            if (i % 2 != 0 && element.precedence == precedence) {
                if (!this.isOperator(element)) {
                    throw new Error("Expected operator in '" + this.originalExpression + "', but got " + element.type);
                }
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
        return this.furtherToken(1);
    }

    furtherToken(increment) {
        return this.index + increment < this.tokens.length ? this.tokens[this.index + increment] : {};
    }

    consumeToken(type) {
        //  doDebug(() => type === CLOSE_BRACKET);
        const token = this.currentToken();
        if (type && token.type !== type) {
            throw new Error("Expected token of type " + type + " in '" + this.originalExpression + "', but got " + token.type);
        }
        this.index++;
        return token;
    }

    currentToken() {
        return this.tokens[this.index];
    }

    parseArray() {
        const array = [];
        this.consumeToken(OPENING_SQUARE_BRACKET);
        while (this.currentToken().type !== CLOSING_SQUARE_BRACKET) {
            if (this.currentToken().type === COMMA) {
                this.consumeToken(COMMA);
            } else {
                array.push(this.parse());
            }
        }
        this.consumeToken(CLOSING_SQUARE_BRACKET);
        return array;
    }

    parseParameters() {
        const array = [];
        var expectCommata = false;
        this.consumeToken(OPEN_BRACKET);
        while (this.currentToken().type !== CLOSE_BRACKET) {
            if (this.currentToken().type === COMMA) {
                if (!expectCommata) {
                    throw new Error("Unexpected comma in '" + this.originalExpression + "'");
                }
                this.consumeToken(COMMA);
                expectCommata = false;
            } else {
                array.push(this.parse());
                expectCommata = true;
            }
        }
        this.consumeToken(CLOSE_BRACKET);
        return array;
    }


    parseFunctionCall() {
        const functionName = this.consumeToken(IDENTIFIER);
        const fct = this.functions[functionName.value];
        const parameters = [];
        if (fct === undefined) {
            throw new Error(`Function ${functionName.value} not found`);
        }
        this.consumeToken(OPEN_BRACKET);
        var expectCommata = false;
        while (this.index < this.tokens.length) {
            if (this.currentToken().type === COMMA) {
                if (!expectCommata) {
                    throw new Error("Unexpected comma in '" + this.originalExpression + "'");
                }
                this.consumeToken(COMMA);
                expectCommata = false;
            } else if (this.currentToken().type === CLOSE_BRACKET) {
                break;
            } else {
                parameters.push(this.parse());
                expectCommata = true;
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
        return new Variable(token.value);
    }

    createConstant(token) {
        return new Constant(token.value);
    }

    createNegativeConstant(token) {
        return new Constant(-token.value);
    }


    createOperator(token) {
        return new Operator(token);
    }

    createPropertyVariable() {
        debugger;
        var variable = this.consumeToken(IDENTIFIER);
        this.consumeToken(OPENING_SQUARE_BRACKET);
        const keyExpression = this.parse();
        this.consumeToken(CLOSING_SQUARE_BRACKET);
        return new ObjectProperty(variable.value, keyExpression);
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

    toString() {
        var str = '(';
        if (this.left) {
            str += this.left.toString();
        } else {
            str += 'unknown';
        }
        str += this.operatorAsString();
        if (this.right) {
            str += this.right.toString();
        } else {
            str += 'undefined';
        }
        str += ')';
        return str;
    }

    operatorAsString() {
        switch (this.type) {
            case ADD: return '+';
            case SUB: return '-';
            case MUL: return '*';
            case DIV: return '/';
            case MOD: return '%';
            case AND: return '&&';
            case OR: return '||';
            case EQUAL: return '===';
            case NOT_EQUAL: return '!==';
            case GREATER: return '>';
            case LESS: return '<';
            case GREATER_EQUAL: return '>=';
            case LESS_EQUAL: return '<=';
            default:
                throw new Error("Unknown operator in '" + this.originalExpression + "': " + this.type);
        }
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
                throw new Error("Unknown operator in '" + this.originalExpression + "': " + token.type);
        }
    }

    getPrecedence(tokenType) {
        switch (tokenType) {
            case MUL:
            case DIV:
            case MOD:
                return 3;
            case ADD:
            case SUB:
                return 2;
            case EQUAL:
            case NOT_EQUAL:
            case GREATER:
            case LESS:
            case GREATER_EQUAL:
            case LESS_EQUAL:
                return 1;
            case AND:
            case OR:
                return 0;
            default:
                return -1;
        }
    }
}

class FunctionCall {
    constructor(fct, parameters) {
        this.type = 'FUNCTION_CALL';
        this.fct = fct;
        this.parameters = parameters;
    }

    evaluate(data) {
        const parameterArray = this.parameters.map(p => p.evaluate(data));
        return this.fct.apply(null, parameterArray);
    }

    toString() {
        return this.fct.name + '(' + this.parameters.map(p => p.toString()).join(', ') + ')';
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

    toString() {
        return this.value;
    }
}

class Variable {
    constructor(path) {
        this.type = 'VARIABLE';
        this.path = path;
    }

    evaluate(data) {
        return data.getValueByPath(this.path);
    }

    toString() {
        return this.path;
    }
}

class NoopAst {
    constructor() {
        this.type = 'NOOP';
    }

    evaluate(data) {
        return '';
    }

    toString() {
        return 'noop';
    }
}

class ObjectProperty {
    constructor(path, key) {
        this.type = 'OBJECT_PROPERTY';
        this.path = path;
        this.key = key;
    }

    evaluate(data) {
        const variable = data.getValueByPath(this.path);
        return variable[this.key.evaluate(data)];
    }

    toString() {
        return '.' + this.key;
    }
}


function doDebug(conditionFkt) {
    if (conditionFkt()) {
        debugger;
    }
}