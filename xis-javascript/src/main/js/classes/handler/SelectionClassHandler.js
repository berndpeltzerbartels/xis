class SelectionClassHandler extends TagHandler {
    constructor(element) {
        super(element);
        this.element = element;
        this.groupHandler = null;
        this.selectionClassExpression = new TextContentParser(this.tag.getAttribute('xis:selection-class')).parse();;
        this.currentSelectionClass = null;
        this.element.addEventListener('click', event => this.onClick(event));
    }

    refresh(data) {
        this.selectionClass = this.selectionClassExpression.evaluate(data) || '';
        this.getGroupHandler().register(this);
        this.refreshDescendantHandlers(data);
    }

    onClick(event) {
        this.currentSelectionClass = this.selectionClass;
        this.getGroupHandler().unselectAll();
        this.element.classList.add(this.selectionClass);
    }

    unselect() {
        if (this.currentSelectionClass) {
            this.element.classList.remove([this.currentSelectionClass]);
        }
    }


    getGroupHandler() {
        if (!this.groupHandler) {
            this.groupHandler = this.findParentSelectionGroupHandler();
        }
        if (!this.groupHandler) {
            throw new Error('SelectionClassHandler: No parent SelectionGroupHandler found.');
        }
        return this.groupHandler;
    }

    findParentSelectionGroupHandler() {
        var handler = this.parentHandler;
        while (handler) {
            if (handler.type == 'selection-group-handler') {
                return handler;
            }
            handler = handler.parentHandler;
        }
        return null;
    }
}
