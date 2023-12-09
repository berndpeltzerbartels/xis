class RootTagHandler extends TagHandler {
    constructor(tag) {
        super(tag);
        this.type = 'root-handler';
    }

    refresh(data) {
        this.refreshDescendantHandlers(data);
    }

    /**
     * 
     * @param {TagHandler} parentHandler 
     */
    bindParentHandler(parentHandler) {
        for (var handler of this.descendantHandlers) {
            parentHandler.addDescendantHandler(handler);
        }
    }



    unbindParentHandler(parentHandler) {
        parentHandler.descendantHandlers = parentHandler.descendantHandlers.filter(h => h != this);
    }

}