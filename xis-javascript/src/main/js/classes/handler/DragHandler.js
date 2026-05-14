class DragHandler extends TagHandler {

    static mimeType() {
        return 'application/x-xis-drag';
    }

    constructor(element) {
        super(element);
        this.type = 'drag-handler';
        this.dragExpressionSource = this.getAttribute('xis:drag');
        this.dragVariableName = undefined;
        this.dragValueExpression = undefined;
        this.parseDragExpression();
        element.setAttribute('draggable', 'true');
        element.addEventListener('dragstart', event => this.onDragStart(event));
    }

    parseDragExpression() {
        const separator = this.dragExpressionSource.indexOf(':');
        if (separator < 1 || separator === this.dragExpressionSource.length - 1) {
            throw new Error('xis:drag must use "name:expression" syntax.');
        }
        this.dragVariableName = this.dragExpressionSource.substring(0, separator).trim();
        if (!/^[A-Za-z_$][A-Za-z0-9_$]*$/.test(this.dragVariableName)) {
            throw new Error('xis:drag variable name must be a simple identifier.');
        }
        this.dragValueExpression = new TextContentParser(this.dragExpressionSource.substring(separator + 1).trim()).parse();
    }

    refresh(data) {
        this.data = data;
        return this.refreshDescendantHandlers(data);
    }

    onDragStart(event) {
        if (!this.data) {
            return;
        }
        const value = this.dragValueExpression.evaluate(this.data);
        const payload = JSON.stringify({ name: this.dragVariableName, value: value });
        const dataTransfer = this.dataTransfer(event);
        dataTransfer.setData(DragHandler.mimeType(), payload);
        dataTransfer.setData('text/plain', value === undefined ? '' : String(value));
    }

    dataTransfer(event) {
        return event.dataTransfer ? event.dataTransfer : event.getDataTransfer();
    }
}
