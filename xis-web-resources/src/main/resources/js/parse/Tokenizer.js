
class Tokenizer {

    tokens(str) {
        var tokens = [];
        var buff = { numbers: false, alpha: false, chars: '', dot: false, negative: false };
        for (var i = 0; i < str.length; i++) {
            var c = str.charAt(i);
            switch (c) {
                case '\\':
                    if (i + 1 < str.length) {
                        buff.chars += str.charAt(++i);
                    } else {
                        buff.chars += '\\'
                    }
                    break;
                case '(':
                    this.endBuff(buff, tokens);
                    tokens.push({ type: 'OPEN_PAR' });
                    break;
                case ')':
                    this.endBuff(buff, tokens);
                    tokens.push({ type: 'CLOSE_PAR' });
                    break;
                case ',':
                    this.endBuff(buff, tokens);
                    tokens.push({ type: 'KOMMA' });
                    break;
                case '.':
                    this.endBuff(buff, tokens);
                    tokens.push({ type: 'DOT' });
                    break;
                case '&': {
                    if (buff.chars.length == 1 && buff.chars.charAt(0) == '&') {
                        tokens.push({ type: 'AND' });
                        this.resetBuffer(buff);
                    } else {
                        this.addTextChar(buff, c);
                    }
                    break;
                }
                case '|': {
                    if (buff.chars.length == 1 && buff.chars.charAt(0) == '|') {
                        tokens.push({ type: 'OR' });
                        this.resetBuffer(buff);
                    } else {
                        this.addTextChar(buff, c);
                    }
                    break;
                }
                case '\'':
                    if (buff.chars.length == 0 && buff.chars.charAt(0) == '\'') {
                        buff.chars += c;
                        this.endBuff(buff, tokens);
                    } else {
                        this.endBuff(buff, tokens);
                        buff.chars += c;
                    }
                    break;
                default: this.addTextChar(buff, c);

            }
        }
        this.endBuff(buff, tokens);
        return tokens;
    }

    resetBuffer(buff) {
        buff.numbers = false;
        buff.alpha = false;
        buff.dot = false;
        buff.negative = false;
        buff.chars = '';
    }


    endBuff(buff, tokens) {
        var rv;
        if (buff.chars != '') {
            if (buff.numbers) {
                if (buff.alpha) {
                    rv = this.addText(buff, tokens);
                } else {
                    rv = this.addNum(buff, tokens);
                }

            } else {
                rv = this.addText(buff, tokens);
            }
        }
        this.resetBuffer(buff);
        return rv;

    }

    addText(buff, tokens) {
        var token;
        if (buff.chars.charAt(buff.length - 1) == '\'' && buff.chars.charAt(0) == '\'') {
            token = { type: 'TEXT', value: buff.chars };
        } else {
            token = { type: 'NAME', value: trim(buff.chars) };
        }
        tokens.push(token);
        return token.type;
    }


    addNum(buff, tokens) {
        var token;
        if (buff.dot) {
            token = { type: 'DECIMAL', value: parseFloat(buff.chars) * buff.negative ? -1 : 1 };
        } else {
            token = { type: 'INT', value: parseInt(buff.chars) * buff.negative ? -1 : 1 };
        }
        tokens.push(token);
        return token.type;
    }

    addTextChar(buff, c) {
        if (c > -1 && c < 10) {
            buff.numbers = true;
            buff.chars += c;
        } else if (c == '.') {
            buff.dot = true;
            buff.chars += c;
        } else if (c == '-' && buff.chars.length == 0) {
            buff.negative = true;
        } else {
            buff.alpha = true;
            buff.chars += c;
        }
    }

}