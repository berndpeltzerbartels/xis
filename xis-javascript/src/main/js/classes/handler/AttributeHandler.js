/**
* @classdesc AttributeHandler class is responsible for handling attribute values containing a variable.
* @class AttributeHandler
* @extends TagHandler
 */
class AttributeHandler extends TagHandler {

    /**
     * @param {Element} element 
     * @param {String} attrName
     * @param {function} onReactiveVariableDetected - Optional callback called when a reactive variable is detected. Receives (context, path, attributeName).
     */
    constructor(element, attrName, onReactiveVariableDetected = null) {
        super(element);
        this.type = 'attribute-handler';
        this.attrName = attrName;
        this.reactiveVariables = [];
        
        const listener = (context, path) => {
            this.addReactiveVariable(context, path);
            if (onReactiveVariableDetected) {
                onReactiveVariableDetected(context, path, this.attrName);
            }
        };
        
        this.attrExpression = new TextContentParser(element.getAttribute(this.attrName), listener).parse();
        this.registerEventListeners();
    }

    addReactiveVariable(context, path) {
        this.reactiveVariables.push({ context, path });
    }

    registerEventListeners() {
        this.reactiveVariables.forEach(variable => {
            app.eventPublisher.addEventListener(EventType.REACTIVE_DATA_CHANGED, () => {
                this.updateAttribute();
            });
        });
    }

    updateAttribute() {
        this.tag.setAttribute(this.attrName, this.attrExpression.evaluate(this.data));
    }

    /**
     * @public
     * @override
     * @param {Data} data
     */
    refresh(data) {
        this.data = data;
        this.updateAttribute();
        return Promise.resolve();
    }
}