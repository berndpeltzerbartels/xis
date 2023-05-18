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
    * this is not a xis-element like e.g. <xis:foreach/>
    * @param {Element} element 
    */
    initializeHtmlElement(element) {
        console.log('initializeHtmlElement:' + element);
        if (element.getAttribute('xis:repeat')) {
            this.initializeRepeat(element);
        }
        if (element.getAttribute('xis:foreach')) {
            this.initializeForeachAttribute(element);
        }
        if (element.getAttribute('xis:page') || element.getAttribute('xis:widget')) {
            this.initializeLink(element);
        }
        if (element.getAttribute('xis:action')) {
            this.initializeActionElement(element);
        }
        this.initializeAttributes(element);
    }

    /**
     * @private
     * @param {Element} element 
     */
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

    initializeActionElement(element) {
        if (element.localName == 'form') {
            this.initializeForm(element);
        } else {
            this.initializeActionLink(element);
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
    initializeActionLink(element) {
        this.addHandler(element, new ActionLinkHandler(element, this.client, this.widgetContainers));
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
        console.log('initializeFrameworkElement:' + element);
        switch (element.localName) {
            case 'xis:foreach':
            case 'xis:forEach':
                this.decorateForeach(element);
                break;
            case 'xis:widget-container':
                this.initializeWidgetContainer(element);
        }
    }

    /**
    * @private
    * @param {Element} element
    */
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
        var arr = doSplit(element.getAttribute('xis:repeat'), ':');
        element.removeAttribute('xis:repeat');
        var foreach = this.createForEach(arr[0], arr[1]);
        this.domAccessor.insertParent(element, foreach);
    }


    /**
    * @private
    * @param {Element} element 
    */
    initializeForeachAttribute(element) {
        var arr = doSplit(element.getAttribute('xis:foreach'), ':');
        element.removeAttribute('xis:foreach');
        var foreach = this.createForEach(arr[0], arr[1]);
        this.domAccessor.insertChild(element, foreach);
    }

    /**
    * @private
    * @param {Element} element 
    */
    initializeContainerAttribute(element) {
        var containerId = element.getAttribute('xis:widget-container');
        var defaultWidget = element.getAttribute('xis:default-widget');
        var containerTag = document.createElement('xis:widget-container');
        if (defaultWidget) {
            containerTag.setAttribute('default-widget', defaultWidget);
        }
        containerTag.setAttribute('id', containerId);
        this.domAccessor.insertChild(element, containerTag);
        this.initializeWidgetContainer(containerTag);
    }

    /**
     * @private
     * @param {Element} element 
     */
    initializeLink(element) {
        this.addHandler(element, new LinkHandler(element));
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
     * @param {Element} container 
     * @returns {Element}
     */
    initializeWidgetContainer(container) {
        this.addHandler(container, new WidgetContainerHandler(container, this.client, this.widgets));
        this.widgetContainers.addContainer(container, container.getAttribute('id')); // TODO validate, the id must not be an expression
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
