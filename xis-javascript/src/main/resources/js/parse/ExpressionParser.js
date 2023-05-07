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