class TokenBracketInserter {
    constructor(tokens, functions) {
        // Parameter functions sind z. B. für Funktionsaufrufe benötigt
        this.tokens = tokens.slice(); // Erzeuge eine Kopie
        this.functions = functions;
    }

    insertBrackets() {
        for (var level = 0; level < 3; level++) {
            this.insertBracketsForLevel(level);
        }
    }

    insertBracketsForLevel(level) {
        for (var i = 0; i < this.tokens.length; i++) {
            var token = this.tokens[i];
            if (token.level === level && this.isOperator(token)) {
                if (i === 0) {
                    throw new Error("No previous element");
                }
                const leftVar = this.tokens[i - 1];
                const rightVar = this.tokens[i++];
                if (!rightVar) {
                    throw new Error("No right element");
                }
                operatorToken = { type: OPERATOR, left: leftVar, right: rightVar };
                this.tokens.splice(i - 1, 3, operatorToken);
            }
        }
    }


    // Liefert einen numerischen Wert für die jeweilige Präzedenz.
    getPrecedence(tokenType) {
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
