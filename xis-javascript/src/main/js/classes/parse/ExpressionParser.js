
class ExpressionParser {

    constructor(functions={}) {
        this.functions = functions;
    }

    parse(expression) {
        debugger;
        const tokens = new ScriptTokenizer(expression).tokenize();
        return new AstFactory(tokens, this.functions, expression).createAst();
    }
}


