
class ExpressionParser2 {

    parse(expression) {
        this.tokens = new ScriptTokenizer(expression).tokenize();
        return new ASTFactory(this.tokens).createAST();
    }
}


