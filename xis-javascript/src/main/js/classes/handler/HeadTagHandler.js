class HeadTagHandler extends TagHandler {

    /**
     * 
     * @param {TagHandlers} tagHandlers 
     */
    constructor(tagHandlers) {
        super(getElementByTagName('head'));
        this.tagHandlers = tagHandlers;
        this.title = getElementByTagName('title');
        this.type = 'head-handler';

    }

    /**
     * @public
     * @param {page} page
     */
    bind(page) {
        this.setTitleExpression(page);
        this.addScriptTags(page);
        if (page.pageAttributes.pageJavascriptSource) {
            this.addPageJavascript(page.pageAttributes.pageJavascriptSource);
        }
        for (var node of this.nodeListToArray(page.headTemplate.childNodes)) {
            if (isElement(node) && node.localName == 'title') {
                continue;
            }
            this.tag.appendChild(node);
        }
        var headTemplateRootHandler = this.tagHandlers.getRootHandler(page.headTemplate);
        this.addDescendantHandler(headTemplateRootHandler);
    }

    /**
    * @private
    * @param {Page} page
    * @param {string} pageSpecificJsSource
    */
    addScriptTags(page) {
        this.scriptSourceExpressions = this.extractFromArrayInPlace(page.headChildArray, node => node.localName == 'script')
            .map(node => node.getAttribute('src'))
            .map(src => new TextContentParser(src, this).parse());
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

    setTitleExpression(page) {
        var arr = this.extractFromArrayInPlace(page.headChildArray, node => node.localName == 'title');
        if (arr.length > 0) {
            var expressionSrc = arr[0].innerText;
            this.titleExpression = new TextContentParser(expressionSrc, this).parse();
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


    /**
     * @param {Array} array
     * @param {Function} filterFunction
     * @returns {Array} An array of all elements matching the filterFunction
     * @description Removes all matching elements from the original array and returns them.
     */
    extractFromArrayInPlace(array, filterFunction) {
        const result = [];
        for (let i = array.length - 1; i >= 0; i--) {
            if (filterFunction(array[i])) {
                result.push(array[i]);
                array.splice(i, 1);
            }
        }
        return result.reverse(); // to preserve original order
    }
}