
const functions = {
};
/**
* Parser for el expression. It should not be used for mixed content (text containing el expressions).
* For mixed content use TextContentParser.
*
* Example expressions:
*   - 42
*   - 'Hello World'
*   - user.name
*   - user.age + 10
*   - user.isActive ? 'Active' : 'Inactive'
*   - max(10, user.age)
*
* Supported operators:
*   - Arithmetic: +, -, *, /, %
*   - Comparison: ==, !=, <, <=, >, >=
*   - Logical: &&, ||
*   - Ternary: condition ? expr1 : expr2
*/

class ExpressionParser {

    constructor(functions={}) {
        this.functions = functions;
    }
    /**
    * Parses an expression and returns its AST representation.
    * @param {string} expression - The expression to parse.
    * @param {function} onReactiveVariableDetected - Optional callback called when a reactive variable is detected. Receives (context, path) where context is 'state'/'localStorage'/'global' and path is the variable path without prefix.
    * @returns {object} The AST representation of the expression.
    */
    parse(expression, onReactiveVariableDetected=null) {
        if (expression && expression.startsWith('${') && expression.endsWith('}')) {
            expression = expression.substring(2, expression.length - 1).trim();
        }
        const tokens = new ScriptTokenizer(expression).tokenize();
        return new AstGenerator(tokens, this.functions, expression, onReactiveVariableDetected).createAst();
    }
}


