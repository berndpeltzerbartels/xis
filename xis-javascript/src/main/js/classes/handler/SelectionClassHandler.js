class SelectionClassHandler extends TagHandler {
    constructor(element) {
        super(element);
        this.element = element;
        this.selectionClassExpression = new TextContentParser(this.tag.getAttribute('xis:selection-class')).parse();;
        this.groupHandler = this.findParentSelectionGroupHandler();
        if (!this.groupHandler) {
           throw new Error('SelectionClassHandler must be inside a selection-group-handler');
        }
        this.currentSelectionClass = null;
        this.element.addEventListener('click', event => this.onClick(event));
    }

    registerToGroup() {
        let parent = this.element.parentElement;
        while (parent) {
            if (parent.hasAttribute && parent.hasAttribute('xis:selection-group') && parent._xisSelectionGroupHandler) {
                this.groupHandler = parent._xisSelectionGroupHandler;
                this.groupHandler.register(this);
                break;
            }
            parent = parent.parentElement;
        }
    }

    refresh(data) {
        this.selectionClass = this.selectionClassExpression.evaluate(data) ||
        this.registerToGroup();
        this.refreshDescendantHandlers(data);
    }

    onClick(event) {
        this.groupHandler.unselectAll();
        this.element.classList.add(this.selectionClass);
        this.currentSelectionClass = this.selectionClass;
    }

    unselect() {
        this.element.classList.remove(this.currentSelectionClass);
    }

    findParentSelectionGroupHandler() {
        var handler = this;
        while (handler) {
            if (handler.type == 'selection-group-handler') {
                return handler;
            }
            handler = handler.parentHandler;
        }
        return null;
    }
}
