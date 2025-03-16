/**
* Parse a script into tokens. Non-variable tokens are returned as objects with a value of strings, integer or float and the type (see constants hereunder).
* Variable tokens are returned as objects with a name. Operators (e.g. '&&') are returned as objects with type (see constants) of the operator.
*/

const STRING = 1;
const INTEGER = 2;
const FLOAT = 3;
const BOOL = 4;
const NULL_OR_UNDEFINED = 5;
const ARRAY = 6;
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

class ScriptTokenizer {

    constructor(script) {
        this.script = script.toString(); // Ensure the script is treated as a string
        this.tokens = [];
    }

    tokenize() {
        let i = 0;
        while (i < this.script.length) {
            let c = this.script.charAt(i);

            // Skip whitespace
            if (/\s/.test(c)) {
                i++;
                continue;
            }

            if (c === '[') {
                let arrayToken = this.readArray(i);
                this.tokens.push({ type: ARRAY, value: arrayToken.value });
                i = arrayToken.newIndex;
                continue;
            }

            // If starting a string literal, use readString
            if (c === '"' || c === "'") {
                const stringToken = this.readString(i);
                this.tokens.push({ type: STRING, value: stringToken.value });
                i = stringToken.newIndex;
                continue;
            }

            // If a digit, read a number (int or float)
            if (/\d/.test(c)) {
                const numberToken = this.readNumber(i);
                this.tokens.push({ type: numberToken.isFloat ? FLOAT : INTEGER, value: numberToken.value });
                i = numberToken.newIndex;
                continue;
            }

            // If a letter or underscore, read identifier/variable or keyword (e.g. true, false, null)
            if (/[a-zA-Z_]/.test(c)) {
                const identToken = this.readIdentifier(i);
                if (identToken.value === "true" || identToken.value === "false") {
                    this.tokens.push({ type: BOOL, value: identToken.value === "true" });
                    i = identToken.newIndex;
                } else if (identToken.value === "null" || identToken.value === "undefined") {
                    this.tokens.push({ type: NULL_OR_UNDEFINED, value: null });
                    i = identToken.newIndex;
                } else {
                    // Otherwise, treat as a generic identifier variable
                    this.tokens.push({ type: IDENTIFIER, name: identToken.value });
                    i = identToken.newIndex;
                }
                continue;
            }

            // Check for two-character operators first
            if (c === '&' && this.script.charAt(i + 1) === '&') {
                this.tokens.push({ type: AND, op: "&&" });
                i += 2;
                continue;
            }
            if (c === '|' && this.script.charAt(i + 1) === '|') {
                this.tokens.push({ type: OR, op: "||" });
                i += 2;
                continue;
            }
            if (c === '!' && this.script.charAt(i + 1) === '=') {
                this.tokens.push({ type: NOT_EQUAL, op: "!=" });
                i += 2;
                continue;
            }
            if (c === '=' && this.script.charAt(i + 1) === '=') {
                this.tokens.push({ type: EQUAL, op: "==" });
                i += 2;
                continue;
            }
            if (c === '>' && this.script.charAt(i + 1) === '=') {
                this.tokens.push({ type: GREATER_EQUAL, op: ">=" });
                i += 2;
                continue;
            }
            if (c === '<' && this.script.charAt(i + 1) === '=') {
                this.tokens.push({ type: LESS_EQUAL, op: "<=" });
                i += 2;
                continue;
            }
            if (c === '+' && this.script.charAt(i + 1) === '+') {
                this.tokens.push({ type: INCREMENT, op: "++" });
                i += 2;
                continue;
            }
            if (c === '-' && this.script.charAt(i + 1) === '-') {
                this.tokens.push({ type: DECREMENT, op: "--" });
                i += 2;
                continue;
            }
            if (c === '+' && this.script.charAt(i + 1) === '=') {
                this.tokens.push({ type: ADD_ASSIGN, op: "+=" });
                i += 2;
                continue;
            }
            if (c === '-' && this.script.charAt(i + 1) === '=') {
                this.tokens.push({ type: SUB_ASSIGN, op: "-=" });
                i += 2;
                continue;
            }
            if (c === '*' && this.script.charAt(i + 1) === '=') {
                this.tokens.push({ type: MUL_ASSIGN, op: "*=" });
                i += 2;
                continue;
            }
            if (c === '/' && this.script.charAt(i + 1) === '=') {
                this.tokens.push({ type: DIV_ASSIGN, op: "/=" });
                i += 2;
                continue;
            }
            if (c === '%' && this.script.charAt(i + 1) === '=') {
                this.tokens.push({ type: MOD_ASSIGN, op: "%=" });
                i += 2;
                continue;
            }

            // If not a two-character operator, handle single-character tokens.
            switch (c) {
                case '!':
                    this.tokens.push({ type: NOT, op: "!" });
                    break;
                case '>':
                    this.tokens.push({ type: GREATER, op: ">" });
                    break;
                case '<':
                    this.tokens.push({ type: LESS, op: "<" });
                    break;
                case '+':
                    this.tokens.push({ type: ADD, op: "+" });
                    break;
                case '-':
                    this.tokens.push({ type: SUB, op: "-" });
                    break;
                case '*':
                    this.tokens.push({ type: MUL, op: "*" });
                    break;
                case '/':
                    this.tokens.push({ type: DIV, op: "/" });
                    break;
                case '%':
                    this.tokens.push({ type: MOD, op: "%" });
                    break;
                case '=':
                    this.tokens.push({ type: ASSIGN, op: "=" });
                    break;
                case '(':
                    this.tokens.push({ type: OPEN_BRACKET, op: "(" });
                    break;
                case ')':
                    this.tokens.push({ type: CLOSE_BRACKET, op: ")" });
                    break;
                default:
                    // For any unrecognized single character, add it as a literal token.
                   throw new Error("Unrecognized character: '"+c+"'");
            }
            i++;
        }

        return this.tokens;
    }

    readString(startIndex) {
        const quoteChar = this.script.charAt(startIndex);
        let i = startIndex + 1;
        let str = '';
        while (i < this.script.length) {
            let c = this.script.charAt(i);
            if (c === '\\') { // handle escape sequences
                str += c;
                i++;
                if (i < this.script.length) {
                    str += this.script.charAt(i);
                }
            } else if (c === quoteChar) {
                i++; // move past the closing quote
                break;
            } else {
                str += c;
            }
            i++;
        }
        return { value: str, newIndex: i };
    }

    readNumber(startIndex) {
        let i = startIndex;
        let numStr = '';
        let isFloat = false;
        while (i < this.script.length && /[0-9.]/.test(this.script.charAt(i))) {
            let c = this.script.charAt(i);
            if (c === '.') {
                isFloat = true;
            }
            numStr += c;
            i++;
        }
        return { value: isFloat ? parseFloat(numStr) : parseInt(numStr, 10), newIndex: i, isFloat };
    }

    readIdentifier(startIndex) {
        let i = startIndex;
        let idStr = '';
        while (i < this.script.length && /[a-zA-Z0-9_\.]/.test(this.script.charAt(i))) {
            idStr += this.script.charAt(i);
            i++;
        }
        return { value: idStr, newIndex: i };
    }

    readArray(startIndex) {
        let i = startIndex + 1;
        let arrayStr = '';
        while (i < this.script.length && this.script.charAt(i) !== ']') {
            arrayStr += this.script.charAt(i);
            i++;
        }
        return { value: arrayStr, newIndex: i + 1 };
    }
}