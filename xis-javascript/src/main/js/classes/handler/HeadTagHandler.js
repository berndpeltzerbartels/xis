class HeadTagHandler extends TagHandler {

    constructor() {
        super(getElementByTagName('head'));
        this.title = getElementByTagName('title');
        this.type = 'head-handler';

    }

    /**
     * @public
     * @param {page} page
     */
    bind(page) {
        this.setTitleExpression(page.titleExpression);
        this.addScriptTags(page.scriptSourceExpressions);
        if (page.pageAttributes.pageJavascriptSource) {
            this.addPageJavascript(page.pageAttributes.pageJavascriptSource);
        }
        for (var node of this.nodeListToArray(page.headTemplate.childNodes)) {
            if (isElement(node) && node.localName == 'title') {
                continue;
            }
            this.tag.appendChild(node);
        }
        this.addDescendantHandler(page.headTemplate._rootHandler);
    }

    /**
    * @private
    * @param {Array} scriptSourceExpressions
    * @param {string} pageSpecificJsSource
    */
    addScriptTags(scriptSourceExpressions) {
        this.scriptSourceExpressions = scriptSourceExpressions;
        for (var scriptSourceExpression of this.scriptSourceExpressions) {
            var scriptElement = document.createElement('script');
            scriptElement.setAttribute('type', 'text/javascript');
            scriptElement.srcexpr = scriptSourceExpression;
            this.tag.appendChild(scriptElement);
        }
    }

    /**
    * @private
    * @param {string} pageJavascriptSource
    */
    addPageJavascript(pageJavascriptSource) {
        var scriptElement = document.createElement('script');
        scriptElement.setAttribute('type', 'text/javascript');
        scriptElement.setAttribute('src', pageJavascriptSource);
        this.tag.appendChild(scriptElement);
    }

    /**
    * Removes all children from head-tag and put them bag to headTemplate, except title.
    *
    * @public
    * @param {Element} headTemplate
    */
    release(headTemplate) {
        for (var node of this.nodeListToArray(this.tag.childNodes)) {
            if (isElement(node) && node.getAttribute('ignore')) {
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
        this.refreshScriptTags(data);
    }

    /**
     * @param {Data} data 
     */
    refreshTitle(data) {
        this.title.innerText = this.titleExpression.evaluate(data);
        innerTextChanged(this.title);
    }

    /**
    * @private
    * @param {Data} data
    */
    refreshScriptTags(data) {
        for (var scriptElement of this.nodeListToArray(this.tag.childNodes)) {
            if (isElement(scriptElement) && scriptElement.localName == 'script') {
                var expression = scriptElement.srcexpr;
                if (expression) {
                    scriptElement.setAttribute('src', expression.evaluate(data));
                    nodeValueChanged(scriptElement);
                }
            }
        }
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