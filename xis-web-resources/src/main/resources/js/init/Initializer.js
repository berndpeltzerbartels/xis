class Initializer {

    /**
     *
     * @param {DomAccessor} domAccessor
     */
    constructor(domAccessor) {
        this.domAccessor = domAccessor;
    }


    initialize(node) {
        console.log('initialize:' + node);
        if (isElement(node) && !node.getAttribute('ignore')) {
            this.initializeElement(node);
        } else {
            this.initializeTextNode(node);
        }
    }

    initializeElement(element) {
        console.log('initializeElement:' + element);
        if (this.isFrameworkElement(element)) {
            this.initializeFrameworkElement(element);
        } else {
            this.initializeHtmlElement(element);
        }
        this.initializeChildNodes(element);
    }

    /**
    * Initializes a html-element, which means 
    * this is not a xis-element like <xis:foreach/>
    * @param {Element} element 
    */
    initializeHtmlElement(element) {
        console.log('initializeHtmlElement:' + element);
        if (element.getAttribute('repeat')) {
            this.initializeRepeat(element);
        }
        if (element.getAttribute('for')) {
            this.initializeFor(element);
        }
        this.initializeAttributes(element);
        element._refresh = function (data) {
            if (this._attributes) {
                for (var attribute of this._attributes) {
                    this.setAttribute(attribute.name, attribute.expression.evaluate(data));
                }
            }
            if (this._handler) {
                this._handler.refresh(data);
            } else {
                for (var i = 0; i < this.childNodes.length; i++) {
                    var child = this.childNodes.item(i);
                    if (child._refresh) {
                        child._refresh(data);
                    }
                }
            }
        }
    }

    initializeAttributes(element) {
        element._attributes = [];
        for (var attrName of element.getAttributeNames()) {
            var attrValue = element.getAttribute(attrName);
            if (attrValue.indexOf('${') != -1) {
                element._attributes.push({
                    name: attrName,
                    expression: new TextContentParser(attrValue).parse()
                });
            }
        }
    }

    /**
     * @private
     * @param {Element} element
     */
    initializeTextNode(node) {
        if (node.nodeValue && node.nodeValue.indexOf('${') != -1) {
            node._expression = new TextContentParser(node.nodeValue).parse();
            node._refresh = function (data) {
                this.nodeValue = this._expression.evaluate(data);
            }
        }
    }

    initializeFrameworkElement(element) {
        console.log('initializeFrameworkElement:' + element);
        switch (element.localName) {
            case 'xis:foreach':
            case 'xis:forEach':
                this.decorateForeach(element);
                break;
            case 'xis:widget-container':
                this.decorateContainer(element);
        }
    }

    initializeChildNodes(element) {
        console.log('initializeChildNodes:' + element);
        for (var index = 0; index < element.childNodes.length; index++) {
            var child = element.childNodes.item(index);
            this.initialize(child);
        }
    }
    /**
    * @private
    * @param {Element} element 
    */
    initializeRepeat(element) {
        console.log('initializeRepeat:' + element);
        var arr = doSplit(element.getAttribute('repeat'), ':');
        element.removeAttribute('repeat');
        var foreach = this.createForEach(arr[0], arr[1]);
        this.domAccessor.insertParent(element, foreach);
    }


    /**
    * @private
    * @param {Element} element 
    */
    initializeFor(element) {
        var arr = doSplit(element.getAttribute('for'), ':');
        element.removeAttribute('for');
        var foreach = this.createForEach(arr[0], arr[1]);
        this.domAccessor.insertChild(element, foreach);
    }

    createForEach(varName, array) {
        var foreach = document.createElement('xis:foreach');
        foreach.setAttribute('var', varName);
        foreach.setAttribute('array', array);
        return this.decorateForeach(foreach);
    }

    decorateForeach(foreach) {
        foreach._handler = new ForeachHandler(foreach, this);
        foreach._refresh = function (data) {
            this._handler.refresh(data);
        }
        return foreach;
    }

    decorateContainer(container) {
        container._handler = new WidgetContainerHandler(container);
        container._refresh = function (data) {
            this._handler.refresh(data);
        }
        return container;
    }


    isFrameworkElement(element) {
        return element.localName.startsWith('xis:');
    }

}
