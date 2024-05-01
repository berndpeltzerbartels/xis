class HeadTagHandler extends TagHandler {

    constructor() {
        super(getElementByTagName('head'));
        this.title = getElementByTagName('title');
        this.type = 'head-handler';

    }

    /**
     * @public
     * @param {Element} headTemplate
     * @param {any} titleExpression
     */
    bind(headTemplate, titleExpression) {
        this.setTitleExpression(titleExpression);
        for (var node of this.nodeListToArray(headTemplate.childNodes)) {
            if (isElement(node) && node.localName == 'title') {
                continue;
            }
            this.tag.appendChild(node);
        }
        this.addDescendantHandler(headTemplate._rootHandler);
    }

    /**
    * Removes all children from head-tag and put them bag to headTemplate, except title.
    *
    * @public
    * @param {Element} headTemplate
    */
    release(headTemplate) {
        for (var node of this.nodeListToArray(this.tag.childNodes)) {
            if (isElement(node) && node.localName == 'title') {
                continue;
            }
            this.tag.removeChild(node);
            headTemplate.appendChild(node);
        }
    }

    /**
     * @public
     * @override
     * @param {Data} data 
     */
    refresh(data) {
        this.refreshTitle(data);
        this.refreshDescendantHandlers(data);
    }

    /**
     * @param {Data} data 
     */
    refreshTitle(data) {
        this.title.innerText = this.titleExpression.evaluate(data);
        innerTextChanged(this.title);
    }

    setTitleExpression(expression) {
        if (expression) {
            this.titleExpression = expression;
        } else {
            this.titleExpression = {
                evaluate(_) { }
            }
        }
    }

    clearTitle() {
        this.innerText = '';
        this.titleExpression = undefined;
        innerTextChanged(this.title);
    }
}