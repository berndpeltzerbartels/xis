/**
 * Factory for creating TagHandlers for DOM tags.
 * (Formerly: Decorator, now Factory pattern)
 */
class HandlerBuilder {

    /**
     * @param {DomAccessor} domAccessor
     * @param {HttpClient} client
     * @param {Widgets} widgets
     * @param {WidgetContainers} widgetContainers
     * @param {TagHandlers} tagHandlers
     */
    constructor(domAccessor, client, widgets, widgetContainers, tagHandlers) {
        this.domAccessor = domAccessor;
        this.client = client;
        this.widgets = widgets;
        this.widgetContainers = widgetContainers;
        this.tagHandlers = tagHandlers;
    }

    /**
     * Creates handlers for a node (recursively for child nodes).
     * @param {Node} node 
     * @param {TagHandler} parentHandler 
     */
    create(node, parentHandler) {
        if (!parentHandler) {
            parentHandler = new RootTagHandler(node);
            this.tagHandlers.mapRootHandler(node, parentHandler);
        }
        if ((isElement(node) && !node.getAttribute('ignore')) || isDocumentFragment(node)) {
            this.createElementHandler(node, parentHandler);
        } else {
            this.createTextNodeHandler(node, parentHandler);
        }
        return parentHandler;
    }

    /**
    * Creates a handler for an element and its attributes/children.
    * @private
    * @param {Element} element
    * @param {TagHandler} parentHandler
    */
    createElementHandler(element, parentHandler) {
        let handler;
        if (element.getAttribute && element.getAttribute('xis:binding') && element.getAttribute('xis:error-class')) {
            // TODO write a test
            parentHandler.addDescendantHandler(new ErrorStyleHandler(element));
        }
        switch (element.localName) {
            case 'xis:raw': {
                // Insert content as HTML (default) or as text if text="true"
                const isText = element.getAttribute('text') === 'true';
                const rawContent = Array.from(element.childNodes).map(n => n.nodeType === 3 ? n.nodeValue : n.outerHTML).join('');
                if (isText) {
                    // Insert as plain text node
                    const textNode = document.createTextNode(rawContent);
                    element.parentNode.replaceChild(textNode, element);
                } else {
                    // Insert as HTML, but keep DOM structure and listeners
                    const parent = element.parentNode;
                    // Füge alle ChildNodes von <xis:raw> vor dem Element ein
                    while (element.firstChild) {
                        parent.insertBefore(element.firstChild, element);
                    }
                    // Entferne das <xis:raw>-Element selbst
                    parent.removeChild(element);
                }
                return;
            }
            case 'xis:foreach':
                handler = parentHandler.addDescendantHandler(this.createForeachHandler(element));
                this.tagHandlers.mapHandler(element, handler);
                return; // Do not evaluate child nodes here!
            case 'xis:widget-container':
                handler = this.createWidgetContainerHandler(element);
                this.tagHandlers.mapHandler(element, handler);
                parentHandler.addDescendantHandler(handler);
                this.createChildNodeHandlers(element, handler);
                return;
            case 'xis:parameter':
                handler = new ParameterTagHandler(element, parentHandler);
                parentHandler.addDescendantHandler(handler);
                break;
            case 'input':
                if (element.getAttribute('xis:binding')) {
                    handler = this.createInputHandler(element);
                }
                break;
            case 'select':
                if (element.getAttribute('xis:binding')) {
                    handler = this.createSelectHandler(element);
                }
                break;
            case 'form':
                if (element.getAttribute('xis:binding')) {
                    handler = this.createFormHandler(element);
                }
                break;
            case 'submit':
                if (element.getAttribute('xis:action')) {
                    handler = this.createSubmitHandler(element);
                }
                break;
            case 'button':
                if (element.getAttribute('xis:action')) {
                    handler = this.createButtonHandler(element);
                }
                break;
            case 'a':
                if (element.getAttribute('xis:page') || element.getAttribute('xis:widget') || element.getAttribute('xis:action')) {
                    handler = this.createLinkHandler(element);
                }
                break;
            case 'xis:message':
                handler = new MessageTagHandler(element);
                break;
            case 'xis:if':
                handler = new IfTagHandler(element);
                break;
        }
        if (isElement(element)) { 
            this.initializeAttributes(element, handler ? handler : parentHandler); 
        }
        if (handler) {
            parentHandler.addDescendantHandler(handler);
            this.tagHandlers.mapHandler(element, handler);
            this.createChildNodeHandlers(element, handler);
        } else {
            this.createChildNodeHandlers(element, parentHandler);
        }
        return handler;
    }

    /**
     * Creates an InputTagHandler.
     * @private
     * @param {Element} element
     */
    createInputHandler(element) {
        switch (element.getAttribute('type')) {
            case 'checkbox':
                return new CheckboxTagHandler(element);
            case 'radio':
                return new RadioTagHandler(element);
            case 'file':
                return null; // TODO new FileInputHandler(element);
            default:
                return new InputTagHandler(element);
        }
    }

    /**
     * Creates a SelectTagHandler.
     * @private
     * @param {Element} element
     */
    createSelectHandler(element) {
        return new SelectTagHandler(element, this.tagHandlers);
    }

    /**
     * Initializes attribute handlers for dynamic attributes.
     * @private
     * @param {Element} element 
     * @param {TagHandler} parentHandler
     */
    initializeAttributes(element, parentHandler) {
        for (let attrName of element.getAttributeNames()) {
            let attrValue = element.getAttribute(attrName);
            if (attrName === 'xis:selection-group') {
                parentHandler.addDescendantHandler(new SelectionGroupHandler(element, attrName));
            }
            else if (attrName === 'xis:selection-class') {
                parentHandler.addDescendantHandler(new SelectionClassHandler(element, attrName));
            }
            else if (attrValue.indexOf('${') !== -1) {
                parentHandler.addDescendantHandler(new AttributeHandler(element, attrName));
            }
        }
    }

    /**
    * Creates a FormHandler.
    * @private
    * @param {Element} formElement 
    */
    createFormHandler(formElement) {
        return new FormHandler(formElement, this.client);
    }

    /**
     * Creates a TextNodeHandler for dynamic text nodes.
     * @private
     * @param {Element} node
     * @param {TagHandler} parentHandler
     */
    createTextNodeHandler(node, parentHandler) {
        if (node.nodeValue && node.nodeValue.indexOf('${') !== -1) {
            let handler = new TextNodeHandler(node);
            parentHandler.addDescendantHandler(handler);
            return handler;
        }
    }

    /**
    * Creates handlers for all child nodes.
    * @private
    * @param {Element} element
    * @param {TagHandler} parentHandler 
    */
    createChildNodeHandlers(element, parentHandler) {
        for (const child of nodeListToArray(element.childNodes)) {
            this.create(child, parentHandler);
        }
    }

    /**
     * Creates a link handler depending on the attribute.
     * <a xis:page..> or <a xis:widget..> or <a xis:action..>
     * @private
     * @param {Element} element
     * @returns {TagHandler}
     */
    createLinkHandler(element) {
        let handler;
        if (element.getAttribute('xis:page')) {
            handler = new PageLinkHandler(element);
        }
        if (element.getAttribute('xis:widget')) {
            handler = new WidgetLinkHandler(element, this.widgetContainers);
        } else if (element.getAttribute('xis:action')) {
            handler = new ActionLinkHandler(element, this.client, this.widgetContainers);
        }
        return handler;
    }

    /**
     * Creates a ForeachHandler.
     * @private
     * @param {Element} foreach 
     * @returns {TagHandler}
     */
    createForeachHandler(foreach) {
        return new ForeachHandler(foreach, this.tagHandlers);
    }

    /**
     * Creates a FormSubmitterHandler.
     * @private
     * @param {Element} element
     */
    createSubmitHandler(element) {
        return new FormSubmitterHandler(element);
    }

    /**
     * Creates an ActionButtonHandler.
     * @private
     * @param {Element} element
     */
    createButtonHandler(element) {
        return new ActionButtonHandler(element, this.client, this.widgetContainers);
    }

    /**
     * Creates a WidgetContainerHandler.
     * @private
     * @param {Element} container 
     * @returns {TagHandler}
     */
    createWidgetContainerHandler(container) {
        return new WidgetContainerHandler(
            container,
            this.widgets,
            this.widgetContainers,
            this.tagHandlers
        );
    }
}