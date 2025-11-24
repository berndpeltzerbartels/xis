class TextNodeHandler extends TagHandler {

    /**
     * @param {Node} node
     * @param {function} onReactiveVariableDetected - Optional callback called when a reactive variable is detected. Receives (context, path).
     */
    constructor(node, onReactiveVariableDetected = null) {
        super(node);
        this.node = node;
        this.reactiveVariables = [];
        
        const listener = (context, path) => {
            this.addReactiveVariable(context, path);
            if (onReactiveVariableDetected) {
                onReactiveVariableDetected(context, path);
            }
        };
        
        this.expression = new TextContentParser(node.nodeValue, listener).parse();
        this.registerEventListeners();
    }

    addReactiveVariable(context, path) {
        this.reactiveVariables.push({ context, path });
    }

    registerEventListeners() {
        this.reactiveVariables.forEach(variable => {
            app.eventPublisher.addEventListener(EventType.REACTIVE_DATA_CHANGED, () => {
                this.updateText();
            });
        });
    }

    updateText() {
        this.node.nodeValue = this.expression.evaluate(this.data);
    }

    /**
     * @public
     * @override
     * @param {Data} data 
     */
    refresh(data) {
        this.data = data;
        this.updateText();
        return Promise.resolve();
    }

}