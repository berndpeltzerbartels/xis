class GlobalMessageTagHandler extends TagHandler {

    constructor(tag) {
        super(tag);
    }

   refresh(data) {
       this.getParentFormHandler().onGlobalMessageHandlerRefreshed(this);
   }

   refreshValidatorMessages(validatorMessages) {
        const messages = validatorMessages.globalMessages;
        if (messages.length > 0) {
            const ul = document.createElement('ul');
            for (const message of messages) {
                const li = document.createElement('li');
                li.innerText = message;
                ul.appendChild(li);
            }
            this.tag.appendChild(ul);
        }
   }


   reset() {
       while (this.tag.firstChild) {
           this.tag.removeChild(this.tag.firstChild);
       }
   }
}