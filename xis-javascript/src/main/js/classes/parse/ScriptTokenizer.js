/**
 * A tokenizer for JavaScript-like scripts.
 */
class ScriptTokenizer {

    /**
     * Creates a new tokenizer for the given script.
     * @param {string} script The script to tokenize.
     */
    constructor(script) {
        this.script = script.toString();
        this.tokens = [];
        this.index = 0;
    }

    tokenize() {
        while (this.index < this.script.length) {
            this.skipWhitespace();
            if (this.index >= this.script.length) break;

            const c = this.peekChar();
            if (this.isColon(c)) {
                this.addToken({ type: COLON });
                this.index++;
            } else if (this.isQuestionMark(c)) {
                this.addToken({ type: QUESTION_MARK });
                this.index++;
            } else if (this.isDot(c)) {
                this.addToken({ type: DOT });
                this.index++;
            } else if (this.isOpeningSquareBracket(c)) {
                this.processArrayStart();
            } else if (this.isClosingSquareBracket(c)) {
                this.processArrayEnd();
            } else if (this.isStringStart(c)) {
                this.processString();
            } else if (this.isDigit(c)) {
                this.processNumber();
            } else if (this.isIdentifierStart(c)) {
                this.processIdentifier();
            } else if (this.isTwoCharOperator(c)) {
                this.processTwoCharOperator();
            } else if (this.isSingleCharOperator(c)) {
                this.processSingleCharOperator();
            } else if (c === ',') {
                this.addToken({ type: COMMA });
                this.index++;
            } else {
                throw new Error("Unrecognized character: '" + c + "' in '" + this.script + "' at position " + this.index);
            }
        }
        return this.tokens;
    }

    // Hilfsmethoden

    skipWhitespace() {
        while (this.index < this.script.length && /\s/.test(this.script.charAt(this.index))) {
            this.index++;
        }
    }

    peekChar(offset = 0) {
        return this.script.charAt(this.index + offset);
    }
    
    addToken(token) {
        token.index = this.tokens.length;
        this.tokens.push(token);
        return token;
    }
    

    // Array-Verarbeitung
    isOpeningSquareBracket(c) {
        return c === '[';
    }

    isClosingSquareBracket(c) {
        return c === ']';
    }

    isQuestionMark(c) {
        return c === '?';
    }

    isDot(c) {
        return c === '.';
    }

    processArrayStart() {
        this.addToken({ type: OPENING_SQUARE_BRACKET, index: this.index });
        this.index++; // Skip '['
    }

    processArrayEnd() {
        this.addToken({ type: CLOSING_SQUARE_BRACKET, index: this.index });
        this.index++; // Skip '['
    }

    // String-Verarbeitung
    isStringStart(c) {
        return c === '"' || c === "'";
    }

    processString() {
        const token = this.readString();
        token.type = STRING;
        this.addToken(token);
    }

    readString() {
        const quoteChar = this.peekChar();
        this.index++; // Skip starting quote
        let str = '';
        while (this.index < this.script.length) {
            let c = this.peekChar();
            if (c === '\\') { // Escapesequenz
                str += c;
                this.index++;
                if (this.index < this.script.length) {
                    str += this.peekChar();
                    this.index++;
                }
            } else if (c === quoteChar) {
                this.index++; // Schließendes Anführungszeichen überspringen
                break;
            } else {
                str += c;
                this.index++;
            }
        }
        return { value: str };
    }

    // Zahlen-Verarbeitung
    isDigit(c) {
        return /\d/.test(c);
    }

    isColon(c) {
        return c === ':';
    }

    processNumber() {
        const token = this.readNumber();
        token.type = token.isFloat ? FLOAT : INTEGER;
        this.addToken(token);
    }

    readNumber() {
        let numStr = '';
        let isFloat = false;
        while (this.index < this.script.length && /[0-9.]/.test(this.peekChar())) {
            let c = this.peekChar();
            if (c === '.') {
                isFloat = true;
            }
            numStr += c;
            this.index++;
        }
        const value = isFloat ? parseFloat(numStr) : parseInt(numStr, 10);
        return { value, isFloat };
    }

    // Identifier und Literale (z. B. true, false)
    isIdentifierStart(c) {
        return /[a-zA-Z_]/.test(c);
    }

    processIdentifier() {
        const token = this.readIdentifier();
        if (token.value === "true" || token.value === "false") {
            token.type = BOOL;
            token.value = token.value === "true";
        } else if (token.value === "null" || token.value === "undefined") {
            token.type = NULL_OR_UNDEFINED;
            token.value = null;
        } else {
            token.type = IDENTIFIER;
            token.name = token.value;
        }
        this.addToken(token);
    }

    readIdentifier() {
        let idStr = '';
        while (this.index < this.script.length && /[a-zA-Z0-9_]/.test(this.peekChar())) {
            idStr += this.peekChar();
            this.index++;
        }
        return { value: idStr};
    }

    // Operatoren

    isTwoCharOperator(c) {
        const nxt = this.peekChar(1);
        return (c === '&' && nxt === '&') ||
               (c === '|' && nxt === '|') ||
               (c === '!' && nxt === '=') ||
               (c === '=' && nxt === '=') ||
               (c === '>' && nxt === '=') ||
               (c === '<' && nxt === '=') ||
               (c === '+' && (nxt === '+' || nxt === '=')) ||
               (c === '-' && (nxt === '-' || nxt === '=')) ||
               (c === '*' && nxt === '=') ||
               (c === '/' && nxt === '=') ||
               (c === '%' && nxt === '=');
    }
    
    processTwoCharOperator() {
        const c = this.peekChar();
        const nxt = this.peekChar(1);
        const op = c + nxt;
        let type;
        switch (op) {
            case "&&": type = AND; break;
            case "||": type = OR; break;
            case "!=": type = NOT_EQUAL; break;
            case "==": type = EQUAL; break;
            case ">=": type = GREATER_EQUAL; break;
            case "<=": type = LESS_EQUAL; break;
            case "++": type = INCREMENT; break;
            case "--": type = DECREMENT; break;
            case "+=": type = ADD_ASSIGN; break;
            case "-=": type = SUB_ASSIGN; break;
            case "*=": type = MUL_ASSIGN; break;
            case "/=": type = DIV_ASSIGN; break;
            case "%=": type = MOD_ASSIGN; break;
            default:
                throw new Error("Unknown two-character operator: " + op);
        }
        this.addToken({ type, op });
        this.index += 2;
    }
    
    isSingleCharOperator(c) {
        return ['!', '>', '<', '+', '-', '*', '/', '%', '=', '(', ')'].includes(c);
    }

    processSingleCharOperator() {
        const c = this.peekChar();
        let type;
        switch(c) {
            case '!': type = NOT; break;
            case '>': type = GREATER; break;
            case '<': type = LESS; break;
            case '+': type = ADD; break;
            case '-': type = SUB; break;
            case '*': type = MUL; break;
            case '/': type = DIV; break;
            case '%': type = MOD; break;
            case '=': type = ASSIGN; break;
            case '(': type = OPEN_BRACKET; break;
            case ')': type = CLOSE_BRACKET; break;
            default:
                throw new Error("Unknown operator: " + c);
        }
        this.addToken({ type, op: c });
        this.index++;
    }
}

// Konstanten für die Token-Typen
const STRING = 1;
const INTEGER = 2;
const FLOAT = 3;
const BOOL = 4;
const NULL_OR_UNDEFINED = 5;
const AND = 9;
const OR = 10;
const NOT = 11;
const EQUAL = 12;
const NOT_EQUAL = 13;
const GREATER = 14;
const GREATER_EQUAL = 15;
const LESS = 16;
const LESS_EQUAL = 17;
const ADD = 18;
const SUB = 19;
const MUL = 20;
const DIV = 21;
const MOD = 22;
const ASSIGN = 23;
const ADD_ASSIGN = 24;
const SUB_ASSIGN = 25;
const MUL_ASSIGN = 26;
const DIV_ASSIGN = 27;
const MOD_ASSIGN = 28;
const INCREMENT = 29;
const DECREMENT = 30;
const OPEN_BRACKET = 31;
const CLOSE_BRACKET = 32;
const IDENTIFIER = 34;
const COMMA = 35;
const OPERATOR = 36;
const OPENING_SQUARE_BRACKET = 37;
const CLOSING_SQUARE_BRACKET = 38;
const QUESTION_MARK = 39;
const COLON = 40;
const DOT = 41;
