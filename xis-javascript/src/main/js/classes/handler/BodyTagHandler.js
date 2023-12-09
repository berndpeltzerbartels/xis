class BodyTagHandler extends TagHandler {

    constructor() {
        super(getElementByTagName('body'));
        this.type = 'body-handler';
        this.attributeHandlers = [];
    }

    refresh(data) {
        this.attributeHandlers.forEach(h => h.refresh(data));
        this.refreshDescendantHandlers(data);
    }

    /**
    * @public
    * @param {Element} bodyTemplate 
    */
    bind(bodyTemplate) {
        for (var node of this.nodeListToArray(bodyTemplate.childNodes)) {
            this.tag.appendChild(node);
        }
        this.addDescendantHandler(bodyTemplate._rootHandler);
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
    * Removes all attributes from body-tag except onload.
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