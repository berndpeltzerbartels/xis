class Initializer {

    /**
     *
     * @param {DomAccessor} domAccessor
     * @param {Client} client
     * @param {Widgets} widgets
     * @param {WidgetContainers} widgetContainers
     */
    constructor(domAccessor, client, widgets, widgetContainers) {
        this.domAccessor = domAccessor;
        this.client = client;
        this.widgets = widgets;
        this.widgetContainers = widgetContainers;
    }


    initialize(node) {
        if (isElement(node) && !node.getAttribute('ignore')) {
            this.initializeElement(node);
        } else {
            this.initializeTextNode(node);
        }
    }

    initializeElement(element) {
        if (this.isFrameworkElement(element)) {
            element = this.initializeFrameworkElement(element); // may be replaced
        } else {
            this.initializeHtmlElement(element);
        }
        this.initializeChildNodes(element);
    }

    /**
    * Initializes a html-element, which means 
    * this is not a xis-element like e.g. <xis:foreach/>
    * @param {Element} element 
    */
    initializeHtmlElement(element) {
        if (element.getAttribute('xis:repeat')) {
            this.initializeRepeat(element);
        }
        if (element.getAttribute('xis:foreach')) {
            this.initializeForeachAttribute(element);
        }
        if (element.getAttribute('xis:page') || element.getAttribute('xis:widget')) {
            this.initializeLinkByAttribute(element);
        }
        if (element.getAttribute('xis:action')) {
            if (element.localName == 'form') {
                this.initializeForm(element);
            } else {
                this.initializeLinkByAttribute(element);
            }
        }
        if (element.getAttribute('xis:widget-container')) {
            this.initializeWidgetContainerByAttribute(element);
        }
        if (element.getAttribute('xis:binding')) {
            this.initializeBinding(element);
        }
        this.initializeAttributes(element);
    }


    initializeBinding(element) {

    }

    /**
     * @private
     * @param {Element} element 
     */
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
    * @param {Element} formElement 
    */
    initializeForm(formElement) {
        // TODO
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

    /**
    * @private
    * @param {Element} element
    */
    initializeFrameworkElement(element) {
        switch (element.localName) {
            case 'xis:foreach':
                return this.decorateForeach(element);
            case 'xis:widget-container':
                return this.initializeWidgetContainer(element);
            case 'xis:a':
                return this.initializeFrameworkLink(element);
            default: return element;
        }
    }

    replaceFrameworkLinkByHtml(element) {
        var replacement = document.createElement('a');
        for (var attrName of element.getAttributeNames()) {
            var attrValue = element.getAttribute(attrName);
            switch (attrName) {
                case 'page':
                case 'widget':
                case 'foreach':
                case 'repeat':
                case 'target-container':
                case 'action': replacement.setAttribute('xis:' + attrName, attrValue);
                default: replacement.setAttribute(attrName, attrValue);
            }
        }
        this.domAccessor.replaceElement(element, replacement);
        this.initializeHtmlElement(replacement);
        for (var child of nodeListToArray(element.childNodes)) {
            element.removeChild(child);
            replacement.appendChild(child);
        }
        return replacement;
    }
    /**
    * @private
    * @param {Element} element
    */
    initializeChildNodes(element) {
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
        var arr = doSplit(element.getAttribute('xis:repeat'), ':');
        var foreach = this.createForEach(arr[0], arr[1]);
        this.domAccessor.insertParent(element, foreach);
        element.removeAttribute('xis:repeat'); // Otherwise endless recursion
    }

    /**
     * <a xis:page..> or 
     * <a xis:widget..>
     * @private
     * @param {Element} element 
     */
    initializeLinkByAttribute(a) {
        a.setAttribute('href', '#');
        var handler;
        if (a.getAttribute('xis:page') || a.getAttribute('xis:widget')) {
            handler = new LinkHandler(a);
        } else if (a.getAttribute('xis:action')) {
            handler = new ActionLinkHandler(a, this.client, this.widgetContainers);
        }
        this.addHandler(a, handler);
        a.onclick = event => handler.onClick(event);
    }

    /**
     * <xis:a....>
     * @private
     * @param {Element} element 
     * @returns {Element} "a"
     */
    initializeFrameworkLink(element) {
        return this.replaceFrameworkLinkByHtml(element);
    }

    /**
    * @private
    * @param {Element} element
    * @returns {Element}
    */
    initializeForeachAttribute(element) {
        var arr = doSplit(element.getAttribute('xis:foreach'), ':');
        var foreach = this.createForEach(arr[0], arr[1]);
        this.domAccessor.insertChild(element, foreach);
        element.removeAttribute('xis:foreach');// Otherwise endless recursion
        return element;
    }

    /**
     * @private
     * @param {string} varName 
     * @param {string} array key for array to iterate
     * @returns {Element} xis:foreach-element
     */
    createForEach(varName, array) {
        var foreach = document.createElement('xis:foreach');
        foreach.setAttribute('var', varName);
        foreach.setAttribute('array', array);
        return this.decorateForeach(foreach);
    }

    /**
     * @private
     * @param {Element} foreach 
     * @returns {Element}
     */
    decorateForeach(foreach) {
        foreach._handler = new ForeachHandler(foreach, this); // never CompositeTagHandler, here
        return foreach;
    }

    /**
      * @private
      * @param {Element} foreach 
      */
    initializeWidgetContainerByAttribute(element) {
        var id = element.getAttribute('xis:widget-container');
        var container = createElement('xis:widget-container');
        container.setAttribute('container-id', id);
        var defaultWidget = element.getAttribute('xis:default-widget');
        if (defaultWidget) {
            container.setAttribute('default-widget', defaultWidget);
        }
        this.domAccessor.insertChild(element, container);
        return container;
    }

    /**
     * @private
     * @param {Element} container 
     * @returns {Element}
     */
    initializeWidgetContainer(container) {
        this.addHandler(container, new WidgetContainerHandler(container, this.client, this.widgets, this.widgetContainers));
        return container;
    }


    /**
     * @private
     * @param {Element} element 
     * @returns {boolean}
     */
    isFrameworkElement(element) {
        return element.localName.startsWith('xis:');
    }

    /**
     * @private
     * @param {Element} element 
     * @param {TagHandler} handler 
     */
    addHandler(element, handler) {
        if (!element._handler) {
            element._handler = handler;
        } else {
            var anotherHandler = element._handler;
            element._handler = new CompositeTagHandler();
            element._handler.addHandler(anotherHandler);
            element._handler.addHandler(handler);
        }
    }

}
