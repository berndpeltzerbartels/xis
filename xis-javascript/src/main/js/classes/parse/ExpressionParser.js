
class ExpressionParser {

    parse(expression) {
        this.tokens = new ScriptTokenizer(expression).tokenize();
        return new AstFactory(this.tokens).createAst();
    }
}


