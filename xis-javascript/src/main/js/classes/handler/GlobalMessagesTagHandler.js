class GlobalMessagesTagHandler extends TagHandler {

    /**
     * 
     * @param {Element} tag 
     */
    constructor(tag) {
        super(tag);
        this.singleMessageTag = undefined;
    }

    /**
     * @public
     * @override
     * @param {Data} data 
     */
    refresh(data) {
        this.refreshDescendantHandlers(data);
        this.singleMessageTag = getFirstChildElement(this.tag);
        if (!this.singleMessageTag) {
            switch (this.tag.tagName) {
                case 'UL':
                case 'OL':
                    this.singleMessageTag = document.createElement('li');
                    break;
                default:
                    this.singleMessageTag = document.createElement('div');
            }
        }
    }

    /**
     * @public
     * @override
     * @param {ValidatorMessages} messages 
     */
    refreshValidatorMessages(messages) {
        this.tag.innerHTML = '';
        for (var message of messages.globalMessages) {
            var messageTag = this.singleMessageTag.cloneNode(true);
            messageTag.innerText = message;
            this.tag.appendChild(messageTag);
        }
    }
}