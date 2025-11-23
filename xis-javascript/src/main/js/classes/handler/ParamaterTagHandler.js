class ParameterTagHandler extends TagHandler {
    constructor(element, parentHandler) {
        super(element);
        if(!parentHandler) {
            throw new Error("ParameterTagHandler must have a parent handler.");
        }
        if (!parentHandler.addParameter) {
            throw new Error("Parent handler must implement addParameter method.");
        }
        this.parentHandler = parentHandler;
    }

    refresh(data) {
        this.data = data;
        const descendantPromise = this.refreshDescendantHandlers(data); // AttributeHandler !
        var name = this.getAttribute('name');
        var value = this.hasAttribute("value") ? this.getAttribute('value') : this.tag.innerText;
        this.parentHandler.addParameter(name, value);
        return descendantPromise;
    }

    reapply() {
        const descendantPromise = this.reapplyDescendantHandlers(); // AttributeHandler !
        var name = this.getAttribute('name');
        var value = this.hasAttribute("value") ? this.getAttribute('value') : this.tag.innerText;
        this.parentHandler.addParameter(name, value);
        return descendantPromise;
    }
}