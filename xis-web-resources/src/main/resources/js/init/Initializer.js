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

    initializeHtmlElement(element) {
        console.log('initializeHtmlElement:' + element);
        if (element.getAttribute('repeat')) {
            this.initializeRepeat(element);
        }
        if (element.getAttribute('for')) {
            this.initializeFor(element);
        }
        element._refresh = function (data) {
            for (var attribute of this._attributes) {
                this.setAttribute(atttribute.name, attribute.expression.evaluate(data));
            }
            for (var i = 0; i < this.childNodes; i++) {
                var child = nodeList.item(i);
                if (child._refresh) {
                    child._refresh(data);
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
                element._handler = new ForeachHandler(element);
                break;
            case 'xis:widget-container':
                element._handler = new WidgetContainerHandler(element);
        }
        element._refresh = function (data) {
            this._handler.refresh(data);
        }
    }

    initializeChildNodes(element) {
        console.log('initializeChildNodes:' + element);
        for (let index = 0; index < element.childNodes.length; index++) {
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
        var foreach = document.createElement('xis:foreach');
        foreach.setAttribute('var', arr[0]);
        foreach.setAttribute('array', arr[1]);
        this.domAccessor.insertParent(element, foreach);
        this.initializeFrameworkElement(foreach);
    }


    /**
    * @private
    * @param {Element} element 
    */
    initializeFor(element) {
        var arr = doSplit(element.getAttribute('repeat'), ':');
        var foreach = document.createElement('xis:foreach');
        foreach.setAttribute('var', arr[0]);
        foreach.setAttribute('array', arr[1]);
        this.domAccessor.insertParent(element, foreach);
        this.initializeFrameworkElement(foreach);
    }



    isFrameworkElement(element) {
        return element.localName.startsWith('xis:');
    }

}
