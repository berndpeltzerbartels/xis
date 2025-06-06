
class ExpressionParser {

    constructor(functions={}) {
        this.functions = functions;
    }

    parse(expression) {
        const tokens = new ScriptTokenizer(expression).tokenize();
        return new AstGenerator(tokens, this.functions, expression).createAst();
    }
}


