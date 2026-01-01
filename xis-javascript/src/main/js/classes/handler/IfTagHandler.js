class IfTagHandler extends TagHandler {
    /**
     * @param {Element} tag
     */
    constructor(tag) {
        super(tag);
        this.type = 'if-tag-handler';
        this.expression = this.expressionFromAttribute('condition');
        
        // --- Anchors ---
        this.startAnchor = document.createComment('xis:if');
        this.endAnchor   = document.createComment('/xis:if');
        
        const parent = tag.parentNode;
        parent.insertBefore(this.startAnchor, tag);
        parent.insertBefore(this.endAnchor, tag.nextSibling);
        
        // Store child nodes before removing tag
        this.childNodes = this.nodeListToArray(tag.childNodes);
        
        // Remove tag completely
        tag.remove();
        
        this.conditionValue = undefined;
    }

    refresh(data) {
        this.data = data;
        const newConditionValue = this.expression.evaluate(data);
        if (this.conditionValue !== newConditionValue) {
            if (newConditionValue) {
                this.linkChildNodes();
            } else {
                this.unlinkChildNodes();
            }
        }
        this.conditionValue = newConditionValue;
        
        if (this.conditionValue) {
            return this.refreshDescendantHandlers(data);
        }
        return Promise.resolve();
    }

    unlinkChildNodes() {
        for (var i = 0; i < this.childNodes.length; i++) {
            const child = this.childNodes[i];
            if (child.parentNode) {
                child.remove();
            }
        }
    }

    linkChildNodes() {
        const parent = this.startAnchor.parentNode;
        for (var i = 0; i < this.childNodes.length; i++) {
            parent.insertBefore(this.childNodes[i], this.endAnchor);
        }
    }

}