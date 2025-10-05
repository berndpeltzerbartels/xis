class IfTagHandler extends TagHandler {
    /**
     * @param {Element} tag
     */
    constructor(tag) {
        super(tag);
        this.type = 'if-tag-handler';
        this.expression = this.expressionFromAttribute('condition');
        this.childNodes = this.nodeListToArray(tag.childNodes);
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

    /**
     * @public
     * @returns {Promise}
     */
    reapply(invoker) {
        const newConditionValue = this.expression.evaluate(this.data);
        if (this.conditionValue !== newConditionValue) {
            if (newConditionValue) {
                this.linkChildNodes();
            } else {
                this.unlinkChildNodes();
            }
            this.conditionValue = newConditionValue;
        }
        if (this.conditionValue) {
            return this.reapplyDescendantHandlers(invoker);
        }
        return Promise.resolve();
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