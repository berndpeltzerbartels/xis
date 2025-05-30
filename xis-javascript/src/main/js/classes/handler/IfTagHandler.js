class IfTagHandler extends TagHandler {
    /**
     * @param {Element} tag
     */
    constructor(tag) {
        super(tag);
        this.type = 'if-tag-handler';
        this.expression = this.expressionFromAttribute('condition');
        this.childNodes = this.nodeListToArray(tag.childNodes);
        this.conditionValue = true;
    }

    refresh(data) {
        debugger;
        var newConditionValue = this.expression.evaluate(data);
        if (this.conditionValue != newConditionValue) {
            if (newConditionValue) {
                this.linkChildNodes();
            } else {
                this.unlinkChildNodes();
            }
        }
        this.conditionValue = newConditionValue;
        if (this.conditionValue) {
            this.refreshDescendantHandlers(data);
        }
    }


    unlinkChildNodes() {
        for (var i = 0; i < this.childNodes.length; i++) {
            this.tag.removeChild(this.childNodes[i]);
        }
    }

    linkChildNodes() {
        for (var i = 0; i < this.childNodes.length; i++) {
            this.tag.appendChild(this.childNodes[i]);
        }
    }

}