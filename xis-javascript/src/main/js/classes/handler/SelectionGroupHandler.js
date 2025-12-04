class SelectionGroupHandler extends TagHandler {
    constructor(groupElement) {
        super(groupElement);
        this.groupElement = groupElement;
        this.selectionHandlers = [];
        this.type = 'selection-group-handler';
    }

    register(handler) {
        this.selectionHandlers.push(handler);
    }

    unselectAll() {
        this.selectionHandlers.forEach(h => h.unselect());
    }

    reset() {
        this.selectionHandlers = [];
    }

    refresh(data) {
        this.reset();
        this.refreshDescendantHandlers(data);
    }

}
