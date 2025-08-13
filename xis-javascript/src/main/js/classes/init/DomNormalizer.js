/**
 * @class DomNormalizer
 * @package classes/init
 * @access public
 * @description
 * Because some tags can get expressed by attributes and by a special element, 
 * we want to normalize the document. One of the reasons to do so is to avoid 
 * a tag handler has to deal with to types of attributes (with and without xis-namespace).
 * 
 * Addidionally, on some cases, we are looking for parent tags. Normalization helps to
 * avoid double search.
 * 
 * The document tree is updated to fulfill the following rules:
 * <ul>
 *  <li>Framework elements get replaced by valid html elements, if necessary for correct rendering</li>
 *  <li>Loop attributes are getting replaced by use of the framework's foreach-tag.</li>
 *  <li>Handlers demanded by a framework attribute arre replaced by framework's child tag.</li>
 * </ul>
 * 
 * @property {Element} root
 * @property {DomAccessor} domAccessor
 */
class DomNormalizer {

    /**
    * 
    * @param {Element} root
    * @param {DomAccessor} domAccessor
     */
    constructor(root, domAccessor) {
        this.root = root;
        this.domAccessor = domAccessor;
    }

    /**
     * @public
     */
    normalize() {
        this.doNormalize(this.root);
    }

    /**
     * @private
     * @param {Element} element 
     */
    doNormalize(element) {
        var normalizedElement = element;
        if (this.isFrameworkLink(element) || this.isFrameworkActionLink(element)) {
            normalizedElement = this.replaceFrameworkLinkByHtml(element);
        } else if (this.isFrameworkForm(element)) {
            normalizedElement = this.replaceFrameworkFormTagByHtml(element);
        } else if (this.isFrameworkInput(element)) {
            normalizedElement = this.replaceFrameworkInputByHtml(element);
        } else if (this.isFrameworkSubmit(element)) {
            normalizedElement = this.replaceFrameworkSubmitByHtml(element);
        } else if (this.isFrameworkButton(element)) {
            normalizedElement = this.replaceFrameworkButtonByHtml(element);
        } else if (this.isFrameworkSelect(element)) {
            normalizedElement = this.replaceFrameworkSelectByHtml(element);
        } else {
            this.normalizeHtmlElement(element);
        }
        for (var child of nodeListToArray(normalizedElement.childNodes)) {
            if (isElement(child)) {
                this.doNormalize(child);
            }
        }
    }

    /**
    * Initializes a html-element, which means 
    * this is not a xis-element like e.g. <xis:foreach/>
    * @private
    * @param {Element} element
    */
    normalizeHtmlElement(element) {
        if (element.getAttribute('xis:repeat')) {
            this.surroundWithForeachTag(element);
            element.removeAttribute('xis:repeat');
        }
        if (element.getAttribute('xis:foreach')) {
            this.embedForeachTag(element);
            element.removeAttribute('xis:foreach');
        }
        if (element.getAttribute('xis:widget-container')) {
            this.initializeWidgetContainerByAttribute(element);
            element.removeAttribute('xis:widget-container');
        }
        if (element.getAttribute('xis:message-for')) {
            this.replaceMessageAttributeByChildMessageElement(element);
            element.removeAttribute('xis:message-for');
        }
        if (element.getAttribute('xis:global-messages')) {
            this.replaceGlobalMessagesAttributeByChildGlobalMessagesElement(element);
            element.removeAttribute('xis:global-messages');
        }
        if (element.getAttribute('xis:if')) {
            this.surroundWithIfTag(element);
            element.removeAttribute('xis:if');
        }
        return element;

    }

    /**
   * @private
   * @param {Element} element 
   * @returns {boolean}
   */
    isFrameworkLink(element) {
        return element.localName == 'xis:a';
    }

    /**
    * @private
    * @param {Element} element 
    * @returns {boolean}
    */
    isFrameworkActionLink(element) {
        return element.localName == 'xis:action';
    }


    /**
   * @private
   * @param {Element} element 
   * @returns {boolean}
   */
    isFrameworkForm(element) {
        return element.localName == 'xis:form';
    }

    /**
     * 
     * @param {Element} element 
     * @returns 
     */
    isFrameworkInput(element) {
        return element.localName == 'xis:input';
    }

    /**
     * @private 
     * @param {Element} element
     * @returns {boolean}
     */
    isFrameworkSubmit(element) {
        return element.localName == 'xis:submit';
    }

    /**
    * @private 
    * @param {Element} element
    * @returns {boolean}
    */
    isFrameworkButton(element) {
        return element.localName == 'xis:button';
    }

    /**
    * @private 
    * @param {Element} element
    * @returns {boolean}
    */
    isFrameworkSelect(element) {
        return element.localName === 'xis:select';
    }

    replaceFrameworkSelectByHtml(frameworkSelect) {
        return this.replaceFrameworkElementByHtml(frameworkSelect, 'select');
    }

    /**
    * @private
    * @param {string} varName 
    * @param {string} array key for array to iterate
    */
    createForeach(varName, array) {
        var foreach = document.createElement('xis:foreach');
        foreach.setAttribute('var', varName);
        foreach.setAttribute('array', array);
        return foreach;
    }

    replaceMessageAttributeByChildMessageElement(element) {
        var message = createElement('xis:message'); // TODO Nur "for" im message Tag
        message.setAttribute('message-for', element.getAttribute('xis:message-for'));
        element.removeAttribute('xis:message');
        this.domAccessor.insertChild(element, message);
        return message;
    }

    replaceGlobalMessagesAttributeByChildGlobalMessagesElement(element) {
        var globalMessages = createElement('xis:global-messages');
        element.removeAttribute('xis:global-messages');
        this.domAccessor.insertChild(element, globalMessages);
        return globalMessages;
    }

    replaceFrameworkLinkByHtml(frameworkLink) {
        return this.replaceFrameworkElementByHtml(frameworkLink, 'a');
    }
    /**
    * 
    * @param {Element} frameworkForm
    * @returns {Element} 
    */
    replaceFrameworkFormTagByHtml(frameworkForm) {
        return this.replaceFrameworkElementByHtml(frameworkForm, 'form');
    }

    replaceFrameworkInputByHtml(frameworkInput) {
        return this.replaceFrameworkElementByHtml(frameworkInput, 'input');
    }

    replaceFrameworkSubmitByHtml(frameworkSubmit) {
        return this.replaceFrameworkElementByHtml(frameworkSubmit, 'submit');
    }
    replaceFrameworkButtonByHtml(frameworkButton) {
        return this.replaceFrameworkElementByHtml(frameworkButton, 'button');
    }

    /**
     * 
     * @param {Element} frameworkElement 
     * @returns {Element} (Anchor)
     */
    replaceFrameworkElementByHtml(frameworkElement, elementName) {
        var anchor = document.createElement(elementName);
        for (var attrName of frameworkElement.getAttributeNames()) {
            var attrValue = frameworkElement.getAttribute(attrName);
            switch (attrName) {
                case 'page':
                case 'binding':
                case 'widget':
                case 'foreach':
                case 'repeat':
                case 'target-container':
                case 'parameters':
                case 'action': anchor.setAttribute('xis:' + attrName, attrValue);
                default: anchor.setAttribute(attrName, attrValue);
            }
        }
        this.domAccessor.replaceElement(frameworkElement, anchor);
        for (var child of nodeListToArray(frameworkElement.childNodes)) {
            frameworkElement.removeChild(child);
            anchor.appendChild(child);
        }
        return anchor;
    }

    /**
     * Creates a xis-if-tag and appends as a child of the
     * given element.
     * 
     * @private
     * @param {Element} element 
     * @returns 
     */
    surroundWithIfTag(element) {
        var ifTag = createElement('xis:if');
        ifTag.setAttribute('condition', element.getAttribute('xis:if'));
        this.domAccessor.insertParent(element, ifTag);
        element.removeAttribute('xis:if'); // Otherwise endless recursion
        return element;
    }


    /**
    * Creates a xis-foreach-tag and appends as a child of the 
    * given element.
    * 
    * @private
    * @param {Element} element 
    */
    embedForeachTag(element) {
        var arr = doSplit(element.getAttribute('xis:foreach'), ':');
        var foreach = this.createForeach(arr[0], arr[1]);
        this.domAccessor.insertChild(element, foreach);
        element.removeAttribute('xis:foreach');// Otherwise endless recursion
        return foreach;
    }

    /**
    * 
    * Creates a xis-foreach-tag and append the element as a child.
    * @private
    * @param {Element} element 
    */
    surroundWithForeachTag(element) {
        var arr = doSplit(element.getAttribute('xis:repeat'), ':');
        var foreach = this.createForeach(arr[0], arr[1]);
        this.domAccessor.insertParent(element, foreach);
        element.removeAttribute('xis:repeat'); // Otherwise endless recursion
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

}