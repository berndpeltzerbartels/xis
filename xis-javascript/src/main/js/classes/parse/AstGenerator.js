/**
 * Create the abstract source tree of the expression in tag attributes or text content.
 */
class AstGenerator {

    /**
     *
     * @param {array<any>} tokens - the tokens of the expression.
     * @param {any} functions - a map of function names to functions that can be used in the expression.
     * @param {string} originalExpression - The original expression string. Used for error messages.
     */
    constructor(tokens, functions, originalExpression) {
        this.tokens = tokens;
        this.functions = functions;
        this.originalExpression = originalExpression;
        this.index = 0;
    }

    /**
     * Create the abstract syntax tree from the tokens.
     * 
     * @returns the root element of the abstract syntax tree. The root element is an operator or fanction call in common cases.
     * If the expression is empty, a NoopAst is returned. If there is only a single element, this element is returned.
     */
    createAst() {
        if (this.tokens.length === 0) {
            return new NoopAst();
        }
        return this.parse();
    }

    /**
     * Parse the expression and create the abstract syntax tree.
     *
     * @returns the root element of the abstract syntax tree. The root element is an operator or fanction call in common cases.
     * If the expression is empty, a NoopAst is returned. If there is only a single element, this element is returned.
     */
    parse() {
        var row = [];
        while (this.index < this.tokens.length) {
            const token = this.tokens[this.index];
            switch (this.currentToken().type) {
                case QUESTION_MARK:
                    return this.createTernaryOperator(row);
                case OPEN_BRACKET:
                    this.consumeToken(OPEN_BRACKET);
                    row.push(this.parse());
                    this.consumeToken(CLOSE_BRACKET);
                    break;
                case COLON:
                case QUESTION_MARK:
                case CLOSE_BRACKET:
                case CLOSING_SQUARE_BRACKET:
                    return this.toExpression(row);
                case IDENTIFIER:
                    if (this.nextToken().type === OPEN_BRACKET) {
                        row.push(this.parseFunctionCall());
                    } else if (this.nextToken().type === OPENING_SQUARE_BRACKET) {
                        row.push(this.createPropertyVariable());
                    } else {
                        const identifierToken = this.consumeToken(IDENTIFIER);
                        row.push(this.createVariable(identifierToken));
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
                    if ((row.length === 0 || this.isOperator(row[row.length - 1]))
                        && ([FLOAT, INTEGER].indexOf(this.nextToken().type) !== -1)) {
                        this.consumeToken(SUB);
                        row.push(this.createNegativeConstant(this.consumeToken()));
                    } else {
                        row.push(this.createOperator(this.consumeToken()));
                    }
                    break;
                case NOT:
                    this.consumeToken(NOT);
                    row.push(new Negation());
                    break;
                case AND:
                case OR:
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

    /**
     * Convert the row of tokens into an expression.
     *
     * @param {array<any>} row
     * @returns the expression created from the row of tokens.
     */
    toExpression(row) {
        this.applyNegation(row);
        switch (row.length) {
            case 0: return [];
            case 1: return row[0];
            default:
                var result;
                for (var precedence = 4; precedence >= 0; precedence--) {
                    const operator = this.expressionForPrecedence(row, precedence);
                    if (operator) {
                        result = operator;
                    }
                }
                return result;
        }
    }

    /**
     * 
     * @param {array<any>} row 
     * @param {Number} precedence 
     * @returns 
     */
    expressionForPrecedence(row, precedence) {
        var i = 1;
        var operator;
        while (i < row.length) {
            var element = row[i];
            if (element.type === 'Negation') {
                negation = element;
                row.splice(i, 1);
                continue;
            }
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

    /**
     * Apply negation to the row of tokens.
     *
     * @param {array<any>} row
     * @throws Error if the row ends with a negation.
     * @returns void
     *  
     * @description This method processes the negation operator in the row of tokens.
     */
    applyNegation(row) {
        var i = 0;
        while (i < row.length) {
            var element = row[i];
            if (element.type === NOT) {
                if (i + 1 >= row.length) {
                    throw new Error("Unexpected end of expression in '" + this.originalExpression + "'. It should not end with negation");
                }
                const nextElement = row[i + 1];
                nextElement.negated = true;
                row.splice(i, 2, nextElement);
            }
            i++;
        }
    }

    /**
     * 
     * @param {array<any>} row 
     */
    processNegation(row) {
        var i = 0;
        while (i < row.length) {
            var element = row[i];
            if (element.type === NOT) {
                const negation = new Negation();
                negation.expression = row[i + 1];
                this.replace(row, i, 2, negation);
            } else {
                i++;
            }
        }
    }

    /**
     * 
     * @param {*} arr 
     * @param {*} startIndex 
     * @param {*} count 
     * @param {*} replacement 
     */
    replace(arr, startIndex, count, replacement) {
        arr.splice(startIndex + 1, count - 1);
        arr[startIndex] = replacement;
    }

    /**
     * Get the next token in the expression.
     *
     * @returns the next token in the expression.
     * @description This method returns the next token in the expression.
     * 
     * */
    nextToken() {
        return this.furtherToken(1);
    }

    /**
     * 
     * @param {*} increment 
     * @returns 
     */
    furtherToken(increment) {
        return this.index + increment < this.tokens.length ? this.tokens[this.index + increment] : {};
    }

    /**
     * Consume the current token and return it.
     *
     * @param {string} type
     * @returns the current token.
     * @throws Error if the current token is not of the expected type.
     */
    consumeToken(type) {
        const token = this.currentToken();
        if (type && token.type !== type) {
            throw new Error("Expected token of type " + type + " in '" + this.originalExpression + "', but got " + token.type);
        }
        this.index++;
        return token;
    }

    /**
     * 
     * @return
     */

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

    /** 
     * Check if the token is an operator.
     *
     * @param {object} token
     * @returns true if the token is an operator, false otherwise.
     * @description This method checks if the token is an operator.
     * 
     * @throws Error if the token is not of the expected type.

    */
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
            case 'TERNARY':
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the token is a constant.
     *
     * @param {object} token
     * @returns true if the token is a constant, false otherwise.
     * @description This method checks if the token is a constant.
     *
     * @throws Error if the token is not of the expected type.
     */
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

    /**
     * 
     * @param {string} token 
     * @returns 
     */
    createVariable(token) {
        const path = token.value;
        
        // Check if this is a special state or localStorage variable
        if (path.startsWith('state.')) {
            const variablePath = path.substring(6); // Remove 'state.' prefix
            return this.createClientStateVariable(variablePath);
        }

        if (path.startsWith('localStorage.')) {
            const variablePath = path.substring(13); // Remove 'localStorage.' prefix
            return this.createLocalStoreVariable(variablePath);
        }

        if (path.startsWith('global.')) {
            const variablePath = path.substring(7); // Remove 'global.' prefix
            return this.createGlobalVariable(variablePath);
        }

        // Default variable for regular data access
        return new Variable(path);
    }

    /**
     * Creates a ClientStateVariable for direct access to client state.
     * @param {string} path - The state path without 'state.' prefix
     * @returns {ClientStateVariable}
     */
    createClientStateVariable(path) {
        return new ClientStateVariable(path);
    }

    /**
     * Creates a LocalStoreVariable for direct access to localStorage.
     * @param {string} path - The localStorage path without 'localStorage.' prefix  
     * @returns {LocalStoreVariable}
     */
    createLocalStoreVariable(path) {
        return new LocalStoreVariable(path);
    }

    /**
     * Creates a GlobalVariable for direct access to global variables.
     * @param {string} path - The global path without 'global.' prefix
     * @returns {GlobalVariable}
     */
    createGlobalVariable(path) {
        return new GlobalVariable(path);
    }

    /**
     * 
     * @param {string} token 
     * @returns 
     */
    createConstant(token) {
        return new Constant(token.value);
    }

    /**
     * 
     * @param {string} token 
     * @returns 
     */
    createNegativeConstant(token) {
        return new Constant(-token.value);
    }

    /**
     * 
     * @param {string} token 
     * @returns 
     */
    createOperator(token) {
        return new Operator(token);
    }

    /**
     * 
     * @returns 
     */
    createPropertyVariable() {
        var variable = this.consumeToken(IDENTIFIER);
        this.consumeToken(OPENING_SQUARE_BRACKET);
        const keyExpression = this.parse();
        this.consumeToken(CLOSING_SQUARE_BRACKET);
        return new ObjectProperty(variable.value, keyExpression);
    }

    /**
     * 
     * @param {*} row 
     * @returns 
     */
    createTernaryOperator(row) {
        const condition = this.toExpression(row);
        this.consumeToken(QUESTION_MARK);
        var trueExpression = this.parse();
        this.consumeToken(COLON);
        var falseExpression = this.parse();
        return new TernaryOperator(condition, trueExpression, falseExpression);
    }

}

/**
 * @class Operator
 * @description This class represents an operator in the AST.
 * It is used to evaluate the operator with the given left and right operands.
 */
class Operator {

    /**
     * 
     * @param {*} token 
     */
    constructor(token) {
        this.type = token.type;
        this.left = undefined;
        this.right = undefined;
        this.negated = false;
        this.binaryFunction = this.operatorFunction(token);
        this.precedence = this.getPrecedence(token.type);
    }

    /**
     * 
     * @param {Data} data 
     * @returns 
     */
    evaluate(data) {
        const leftValue = this.left.evaluate(data);
        const rightValue = this.right.evaluate(data);
        const rv = this.binaryFunction(leftValue, rightValue);
        return this.negated ? !rv : rv;
    }

    /**
     * 
     * @returns 
     */
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

    /**
     * 
     * @returns 
     */
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


    /**
     * 
     * @param {*} token 
     * @returns 
     */
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

    /**
     * Get the precedence of the operator.
     *  
     * @param {string} tokenType
     * @returns the precedence of the operator.
     * @description This method returns the precedence of the operator.
     */
    getPrecedence(tokenType) {
        switch (tokenType) {
            case MUL:
            case DIV:
            case MOD:
                return 4;
            case ADD:
            case SUB:
                return 3;
            case EQUAL:
            case NOT_EQUAL:
            case GREATER:
            case LESS:
            case GREATER_EQUAL:
            case LESS_EQUAL:
                return 2;
            case AND:
            case OR:
                return 1;
            case 'TERNARY':
                return 0;
            default:
                return -1;
        }
    }
}

/**
 * @class FunctionCall
 * @description This class represents a function call in the AST.
 * It is used to evaluate the function with the given parameters.
 */
class FunctionCall {
    /**
     * @param {function} fct
     * @param {array<any>} parameters
     * @description The function to be called and the parameters to be passed to it.
     */
    constructor(fct, parameters) {
        this.type = 'FUNCTION_CALL';
        this.fct = fct;
        this.parameters = parameters;
        this.negated = false;
    }

    /**
     * @public
     * @param {Data} data
     * @returns {string}
     */
    evaluate(data) {
        const parameterArray = this.parameters.map(p => p.evaluate(data));
        const rv = this.fct.apply(null, parameterArray);
        return this.negated ? !rv : rv;
    }

    /**
     * @public
     * @returns {string}
     */
    toString() {
        return this.fct.name + '(' + this.parameters.map(p => p.toString()).join(', ') + ')';
    }
}

class Constant {
    constructor(value) {
        this.type = 'CONSTANT';
        this.value = value;
        this.negated = false;
    }

    evaluate(data) {
        return this.negated ? !this.value : this.value;
    }

    toString() {
        return this.value;
    }
}

class Variable {
    /**
     * 
     * @param {string} path 
     */
    constructor(path) {
        this.type = 'VARIABLE';
        this.path = path;
        this.negated = false;
    }

    /**
     * 
     * @param {Data} data 
     * @returns 
     */
    evaluate(data) {
        const value = data.getValueByPath(this.path);
        return this.negated ? !value : value;
    }

    /**
     * @public
     * @returns {string}
     */
    toString() {
        return this.path;
    }
}


/**
 * @class NoopAst
 * @description This class represents a no-operation AST node.
 * It is used when the expression is empty or when there is no valid AST node.
 */
class NoopAst {
    constructor() {
        this.type = 'NOOP';
    }

    /**
     * @public
     * @param {Data} data
     * @returns {string}
     */
    evaluate(data) {
        return '';
    }

    /**
     * @public
     * @returns {string}
     */
    toString() {
        return 'noop';
    }
}

/**
 * @class ObjectProperty
 * @description This class represents an object property in the AST.
 */
class ObjectProperty {
    /**
     * @param {string} path
     * @param {string} key
     */
    constructor(path, key) {
        this.type = 'OBJECT_PROPERTY';
        this.path = path;
        this.key = key;
        this.negated = false;
    }

    /**
     * @public
     * @param {Data} data
     * @returns {string}
     */
    evaluate(data) {
        const variable = data.getValueByPath(this.path);
        const rv = variable[this.key.evaluate(data)];
        return this.negated ? !rv : rv;
    }

    /**
     * @public
     * @returns {string}
     */
    toString() {
        return '.' + this.key;
    }
}

/**
 * @class Negation
 * @description This class represents a negation in the AST.
 */
class Negation {
    constructor() {
        this.type = NOT;
        this.expression = undefined;
    }

    /**
     * @public
     * @param {Data} data
     * @returns {string}
     */
    evaluate(data) {
        return !this.expression.evaluate(data);
    }

    /**
     * 
     * @return
     */
    toString() {
        return '!' + this.expression.toString();
    }
}

/**
 * @class TernaryOperator
 * @description This class represents a ternary operator in the AST.
 */
class TernaryOperator {
    constructor(condition, trueExpression, falseExpression) {
        this.type = 'TERNARY';
        this.condition = condition;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;
        this.negated = false;
    }

    /**
     * @public
     * @param {Data} data
     * @returns {string}
     */
    evaluate(data) {
        const conditionValue = this.condition.evaluate(data);
        const rv = conditionValue ? this.trueExpression.evaluate(data) : this.falseExpression.evaluate(data);
        return this.negated ? !rv : rv;
    }

    /**
     * @public
     * @returns {string}
     */
    toString() {
        return this.condition.toString() + ' ? ' + this.trueExpression.toString() + ' : ' + this.falseExpression.toString();
    }
}

/**
 * Variable that accesses client state directly from the store.
 * This bypasses the Data object configuration and allows access to any client state value.
 */
class ClientStateVariable {
    constructor(path) {
        this.path = path;
    }

    evaluate(data) {
        return app.clientState.getValue(this.path);
    }

    toString() {
        return `\${state.${this.path}}`;
    }
}

/**
 * Variable that accesses localStorage directly from the store.  
 * This bypasses the Data object configuration and allows access to any localStorage value.
 */
class LocalStoreVariable {
    constructor(path) {
        this.path = path;
    }

    evaluate(data) {
        return app.localStorage.getValue(this.path);
    }

    toString() {
        return `\${localStorage.${this.path}}`;
    }
}

/**
 * Variable that accesses global variables directly from the global store.
 * Global variables are temporary and cleared at the end of each request processing.
 * This allows sharing data between widgets during a single request.
 */
class GlobalVariable {
    constructor(path) {
        this.path = path;
    }

    evaluate(data) {
        return app.globals.getValue(this.path);
    }

    toString() {
        return `\${global.${this.path}}`;
    }
}


function doDebug(conditionFkt) {
    if (conditionFkt()) {
        debugger;
    }
}