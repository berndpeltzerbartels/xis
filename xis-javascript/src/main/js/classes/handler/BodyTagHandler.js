class BodyTagHandler extends TagHandler {

    /**
     * @param {TagHandlers} tagHandlers 
     */
    constructor(tagHandlers) {
        super(getElementByTagName('body'));
        this.tagHandlers = tagHandlers;
        this.type = 'body-handler';
        this.attributeHandlers = [];
    }

    refresh(data, formData) {
        this.attributeHandlers.forEach(h => h.refresh(data));
        this.refreshDescendantHandlers(data, formData);
    }

    /**
    * @public
    * @param {Element} bodyTemplate
    */
    bind(bodyTemplate) {
        for (var node of this.nodeListToArray(bodyTemplate.childNodes)) {
            bodyTemplate.removeChild(node);
            this.tag.appendChild(node);
        }
        var bodyTemplateHandler = this.tagHandlers.getRootHandler(bodyTemplate);
        this.addDescendantHandler(bodyTemplateHandler);
    }

    /**
    * Removes all children from body-tag and put them bag to bodyTemplate.
    *
    * @public
    * @param {Element} bodyTemplate
    */
    release(bodyTemplate) {
        for (var node of this.nodeListToArray(this.tag.childNodes)) {
            this.tag.removeChild(node);
            bodyTemplate.appendChild(node);
        }
        this.descendantHandlers = [];
    }
    /**
    * @public
    * @param {any} attributes 
    */
    bindAttributes(attributes) {
        for (var name of Object.keys(attributes)) {
            var attribute = attributes[name];
            if (attribute.indexOf('${') != -1) {
                this.attributeHandlers.push(new AttributeHandler(this.tag, name));
            } else {
                this.body.setAttribute(name, attributes[name]);
            }

        }
    }

    /**
    * Removes all attributes from body-tag, except onload.
    *
    * @public
    */
    clearAttributes() {
        for (var name of this.tag.getAttributeNames()) {
            if (name == 'onload') {
                continue;
            }
            this.tag.removeAttribute(name);
        }
        this.attributeHandlers = [];
    }
}