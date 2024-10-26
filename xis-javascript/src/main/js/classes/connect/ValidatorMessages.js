/**
 * @property {array<string>} messages
 * @property {string: string} globalMessages
 */
class ValidatorMessages {
    constructor(obj) {
        if (obj === undefined) {
            this.messages = [];
            this.globalMessages = {};
            return;
        } else {
            this.messages = obj.messages || [];
            this.globalMessages = obj.globalMessages || {};
        }
    }

    isEmpty() {
        return Object.keys(this.messages).length === 0 && this.globalMessages.length === 0;
    }

    getMessageFor(binding) {
        return this.messages[binding] ? this.messages[binding] : '';
    }

}