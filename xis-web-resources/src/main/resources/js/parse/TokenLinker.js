
class TokenLinker {

    constructor(tokens) {
        this.tokens = tokens;
    }

    linkTokens() {
        var root = this.tokens.shift();
        this.evaluate(root);
        return root;
    }

    evaluateParentheses(openPar) {
        var token = this.tokens.shift();
        while (token) {
            if (!openPar.subTree) {
                openPar.subTree = token;
            }
            if (token.type == 'OPEN_PAR') {
                token.next = this.evaluateParentheses(token);
                token = token.next;
            }

            var next = this.tokens.shift();
            if (next.type == 'CLOSE_PAR') {
                openPar.next = next;
                return next;
            }
            token.next = next;
            token = next;
        }
    }

    evaluate(token) {
        while (token) {
            if (token.type == 'OPEN_PAR') {
                token = this.evaluateParentheses(token);
            }
            var next = this.tokens.shift();
            token.next = next;
            token = next;
        }
    }

}