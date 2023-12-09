/**
 * Because some tags can get expressed by attributes, we want to normalize the document.
 * The document tree is updated to fulfill the following rules:
 * <ul>
 *  <li>Framework elements get replaced by valid html elements, if necessary for correct rendering</li>
 *  <li>Loop attributes are getting replaces by use of the framework's foreach-tag.</li>
 * </ul>
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
        if (this.isFrameworkLink(element) || this.isFrameworkActionLink(element)) {
            this.replaceFrameworkLinkByHtml(element);
        } else if (this.isFrameworkForm(element)) {
            this.replaceFrameworkFormTagByHtml(element);
        } else {
            this.normalizeHtmlElement(element);
        }
        for (var child of nodeListToArray(element.childNodes)) {
            if (isElement(child)) {
                this.doNormalize(child);
            }
        }
    }

    /**
    * Initializes a html-element, which means 
    * this is not a xis-element like e.g. <xis:foreach/>
    * @param {Element} element
    */
    normalizeHtmlElement(element) {
        if (element.getAttribute('xis:repeat')) {
            return this.surroundWithForeachTag(element);
        }
        if (element.getAttribute('xis:foreach')) {
            this.embedForeachTag(element);
        }
        if (element.getAttribute('xis:widget-container')) {
            this.initializeWidgetContainerByAttribute(element);
        }
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

    /**
     * 
     * @param {Element} frameworkLink 
     * @returns {Element} (Anchor)
     */
    replaceFrameworkLinkByHtml(frameworkLink) {
        var anchor = document.createElement('a');
        for (var attrName of frameworkLink.getAttributeNames()) {
            var attrValue = frameworkLink.getAttribute(attrName);
            switch (attrName) {
                case 'page':
                case 'widget':
                case 'foreach':
                case 'repeat':
                case 'target-container':
                case 'action': anchor.setAttribute('xis:' + attrName, attrValue);
                default: anchor.setAttribute(attrName, attrValue);
            }
        }
        this.domAccessor.replaceElement(frameworkLink, anchor);
        for (var child of nodeListToArray(frameworkLink.childNodes)) {
            frameworkLink.removeChild(child);
            anchor.appendChild(child);
        }
        return anchor;
    }


    /**
    * 
    * @param {Element} frameworkForm
    * @returns {Element} 
    */
    replaceFrameworkFormTagByHtml(frameworkForm) {
        var form = document.createElement('form');
        form.setAttribute('xis:action', frameworkForm.getAttribute('action'))
        this.domAccessor.replaceElement(frameworkForm, form);
        this.normalizeHtmlElement(form);
        for (var child of nodeListToArray(frameworkForm.childNodes)) {
            frameworkLink.removeChild(child);
            form.appendChild(child);
        }
        return form;
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
        return element;
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