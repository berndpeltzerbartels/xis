/**
 * A handler for all elements with conditioned visiblility.
 * This handler has no parent handler.
 */
class RootTagHandler extends TagHandler {

    constructor(tag) {
        super(tag);
    }

    /**
     * @public
     * @param {Data} data 
     */
    refresh(data) {
        this.refreshDescendantHandlers(data);
    }

    /**
     * TODO : seems to be never used. Check for bug.
     * @param {TagHandler} parentHandler 
     */
    bindParentHandler(parentHandler) {
        handler.parentHandler = this;
        for (var handler of this.descendantHandlers) {
            parentHandler.addDescendantHandler(handler);
        }
    }


    /**
     * TODO : seems to be never used. Check for bug.
     * @param {TagHandler} parentHandler 
     */
    unbindParentHandler(parentHandler) {
        parentHandler.descendantHandlers = parentHandler.descendantHandlers
        .map(h => h.parentHandler = undefined)
        .filter(h => h != this);
    }

}