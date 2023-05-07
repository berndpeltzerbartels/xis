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
        if (element.getAttribute('foreach')) {
            this.initializeForeachAttribute(element);
        }
        if (element.getAttribute('page-link')) {
            this.initializePagelink(element);
        }
        this.initializeAttributes(element);
    }

    initializeAttributes(element) {
        console.log('initializeAttributes:' + element);
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
    initializeForeachAttribute(element) {
        var arr = doSplit(element.getAttribute('foreach'), ':');
        element.removeAttribute('foreach');
        var foreach = this.createForEach(arr[0], arr[1]);
        this.domAccessor.insertChild(element, foreach);
    }


    /**
     * @private
     * @param {Element} element 
     */
    initializePagelink(element) {
        var pageId = element.getAttribute('page-link');
        if (element.localName == 'a') {
            element.setAttribute('href', '#');
        }
        element.onclick = e => bindPage(pageId);
    }

    createForEach(varName, array) {
        var foreach = document.createElement('xis:foreach');
        foreach.setAttribute('var', varName);
        foreach.setAttribute('array', array);
        return this.decorateForeach(foreach);
    }

    decorateForeach(foreach) {
        foreach._handler = new ForeachHandler(foreach, this);
        return foreach;
    }

    decorateContainer(container) {
        container._handler = new WidgetContainerHandler(container);
        return container;
    }


    isFrameworkElement(element) {
        return element.localName.startsWith('xis:');
    }

}
