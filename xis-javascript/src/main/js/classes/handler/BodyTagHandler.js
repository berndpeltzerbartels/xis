class BodyTagHandler extends TagHandler {

    /**
     * @param {TagHandlers} tagHandlers
     */
    constructor(tagHandlers) {
        super(getElementByTagName('body'));
        this.tagHandlers = tagHandlers;
        this.type = 'body-handler';
        this.attributeHandlers = [];
        this.persistentNodes = { start: [], end: [] };

        // Find, remove, and store persistent nodes from the body once at initialization.
        const nodesToScan = this.nodeListToArray(this.tag.childNodes);
        for (const node of nodesToScan) {
            if (node.nodeType === Node.ELEMENT_NODE && node.getAttribute('ignore') === 'true') {
                // Remove from the live DOM
                this.tag.removeChild(node);
                // Store for later use
                if (node.getAttribute('ignore-position') === 'start') {
                    this.persistentNodes.start.push(node);
                } else { // Default or ignore-position="end"
                    this.persistentNodes.end.push(node);
                }
            }
        }
    }

    refresh(data, formData) {
        this.data = data;
        this.attributeHandlers.forEach(h => h.refresh(data));
        this.refreshDescendantHandlers(data, formData);
    }

    /**
    * Rebuilds the body with persistent nodes and new content from the template.
    * @public
    * @param {Element} bodyTemplate Contains the new content.
    */
    bind(bodyTemplate) {
        // 1. Add persistent "start" nodes to the empty body.
        this.persistentNodes.start.forEach(node => this.tag.appendChild(node));

        // 2. Move new content from the template into the body.
        const newContentNodes = this.nodeListToArray(bodyTemplate.childNodes);
        newContentNodes.forEach(node => {
            bodyTemplate.removeChild(node);
            this.tag.appendChild(node);
        });

        // 3. Add persistent "end" nodes.
        this.persistentNodes.end.forEach(node => this.tag.appendChild(node));

        // 4. Set up handlers for the new content.
        var bodyTemplateHandler = this.tagHandlers.getRootHandler(bodyTemplate);
        this.addDescendantHandler(bodyTemplateHandler);
    }

    /**
    * Moves all dynamic children from the body-tag back to the bodyTemplate.
    * The body will be empty after this call.
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
                this.tag.setAttribute(name, attributes[name]);
            }
        }
    }

    /**
    * Removes all attributes from body-tag, except onload.
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