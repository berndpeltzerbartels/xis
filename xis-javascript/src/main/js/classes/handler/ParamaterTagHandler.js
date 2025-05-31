class ParameterTagHandler extends TagHandler {
    constructor(element, parentHandler) {
        super(element);
        this.parentHandler = parentHandler;
    }

    refresh(data) {
        this.refreshDescendantHandlers(data); // AttributeHandler !
        var name = this.getAttribute('name');
        var value = this.hasAttribute("value") ? this.getAttribute('value') : this.tag.innerText;
        this.parentHandler.addParameter(name, value);
    }
}