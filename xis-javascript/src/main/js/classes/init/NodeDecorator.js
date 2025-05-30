/**
 * Decorates the dom tag with handlers. 
 */
// TODO rename to factory
class NodeDecorator {

    /**
     *
     * @param {DomAccessor} domAccessor
     * @param {HttpClient} client
     * @param {Widgets} widgets
     * @param {WidgetContainers} widgetContainers
     * @param {TagHandlers} tagHandlers
     */
    constructor(domAccessor, client, widgets, widgetContainers, tagHandlers) {
        this.domAccessor = domAccessor;
        this.client = client;
        this.backendService = new BackendService(this.client);
        this.widgets = widgets;
        this.widgetContainers = widgetContainers;
        this.tagHandlers = tagHandlers;
    }

    /**
     * 
     * @param {Node} node 
     * @param {TagHandler} parentHandler 
     */
    decorate(node, parentHandler) {
        if (!parentHandler) {
            parentHandler = new RootTagHandler(node);
            this.tagHandlers.mapRootHandler(node, parentHandler);
        }
        if (isElement(node)) {
            if (!node.getAttribute('ignore')) {
                this.decorateElement(node, parentHandler);
            }
        } else {
           this.decorateTextNode(node, parentHandler);
        }
        return parentHandler;
    }

    /**
    * @private
    * @param {Element} element
    * @param {TagHandler} parentHandler
    */
    decorateElement(element, parentHandler) {
        var handler;
        if (element.getAttribute('xis:binding') && element.getAttribute('xis:error-class')) {
            // TODO write a test
            parentHandler.addDescendantHandler(new ErrorStyleHandler(element));
        }
        switch (element.localName) {
            case 'xis:foreach':
                handler = parentHandler.addDescendantHandler(this.decorateForeach(element));
                this.tagHandlers.mapHandler(element, handler);
                return; // Do not evaluate child nodes, here !
            case 'xis:widget-container':
                handler = this.decorateWidgetContainer(element);
                this.tagHandlers.mapHandler(element, handler);
                parentHandler.addDescendantHandler(handler);
                this.decorateChildNodes(element, handler);
                return;
            case 'xis:parameter':
                handler = new ParameterTagHandler(element, parentHandler);
                parentHandler.addDescendantHandler(handler);
                break;
            case 'input': if (element.getAttribute('xis:binding')) {
                handler = this.decorateInputElement(element);
            }
                break;
            case 'form': if (element.getAttribute('xis:binding')) {
                handler = this.decorateForm(element);
            }
                break;
            case 'submit': if (element.getAttribute('xis:action')) {
                handler = this.decorateSubmitElement(element);
            }
                break;
            case 'button': if (element.getAttribute('xis:action')) {
                handler = this.decorateSubmitElement(element);
            }
                break;
            case 'a': if (element.getAttribute('xis:page') || element.getAttribute('xis:widget') || element.getAttribute('xis:action')) {
                handler = this.decorateLinkByAttribute(element);
            }
                break;
            case 'xis:message':
                handler = new MessageTagHandler(element);
                break;
            case 'xis:global-messages':
                handler = new GlobalMessagesTagHandler(element);
                break;
            case 'xis:if':
                handler = new IfTagHandler(element);
                break; 
        }

        this.initializeAttributes(element, handler ? handler : parentHandler);
        if (handler) {
            parentHandler.addDescendantHandler(handler);
            this.tagHandlers.mapHandler(element, handler);
            this.decorateChildNodes(element, handler);
        } else {
            this.decorateChildNodes(element, parentHandler);
        }
        return handler;
    }

    decorateInputElement(element) {
        return new InputTagHandler(element);
    }

    /**
     * @private
     * @param {Element} element 
     * @param {TagHandler} parentHandler
     */
    initializeAttributes(element, parentHandler) {
        element._removedAttributes = {}; // we need removed attributes to clone an element
        for (var attrName of element.getAttributeNames()) {
            var attrValue = element.getAttribute(attrName);
            if (attrValue.indexOf('${') != -1) {
                parentHandler.addDescendantHandler(new AttributeHandler(element, attrName));
                element.removeAttribute(attrName);
                element._removedAttributes[attrName] = attrValue;
            }

        }
    }

    /**
    * @private
    * @param {Element} formElement 
    */
    decorateForm(formElement) {
        return new FormHandler(formElement, this.client);
    }

    /**
     * @private
     * @param {Element} element
     * @param {TagHandler} parentHandler
     */
    decorateTextNode(node, parentHandler) {
        if (node.nodeValue && node.nodeValue.indexOf('${') != -1) {
            var handler = new TextNodeHandler(node);
            parentHandler.addDescendantHandler(handler);
            return handler;
        }
    }

    /**
    * @private
    * @param {Element} element
    * @param {TagHandler} parentHandler 
    */
    decorateChildNodes(element, parentHandler) {
        for (var index = 0; index < element.childNodes.length; index++) {
            var child = element.childNodes.item(index);
            this.decorate(child, parentHandler);
        }
    }

    /**
     * <a xis:page..> or 
     * <a xis:widget..>
     * @private
     * @param {Element} element
     * @returns {TagHandler}
     */
    decorateLinkByAttribute(element) {
        var handler;
        if (element.getAttribute('xis:page')) {
            handler = new PageLinkHandler(element);
        }
        if (element.getAttribute('xis:widget')) {
            handler = new WidgetLinkHandler(element);
        } else if (element.getAttribute('xis:action')) {
            handler = new ActionLinkHandler(element, this.client, this.widgetContainers);
        }
        return handler;
    }

    /**
     * @private
     * @param {Element} foreach 
     * @returns {TagHandler}
     */
    decorateForeach(foreach) {
        return new ForeachHandler(foreach, this.tagHandlers); // never CompositeTagHandler, here
    }

    decorateSubmitElement(element) {
        return new FormSubmitterHandler(element);
    }

    /**
     * @private
     * @param {Element} container 
     * @returns {TagHandler}
     */
    decorateWidgetContainer(container) {
        var handler = new WidgetContainerHandler(container,
            this.backendService,
            this.widgets,
            this.widgetContainers,
            this.tagHandlers);
        return handler;
    }
}