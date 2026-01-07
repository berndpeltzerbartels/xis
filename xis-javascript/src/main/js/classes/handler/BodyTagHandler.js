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
        this.data = undefined;
        this.buffer = undefined;

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
    /**
     * Commits the buffer to the real body element.
     * Removes all non-persistent children and inserts the buffer between persistent start and end nodes.
     * @public
     * @returns {Promise<void>} 
    */
    commitBuffer() {
        return new Promise((resolve, _) => {
            while (this.buffer.firstChild) {
                this.tag.appendChild(this.buffer.firstChild);
            }
            resolve();
        });
    }


    /**
     * @returns {Promise<void>}
     */
     // TODO : Is this used anywhere?
    initBuffer() {
        return new Promise((resolve, _) => {
            // Create a DocumentFragment as buffer
            this.buffer = document.createDocumentFragment();
            // Add persistent start nodes
            for (const node of this.persistentNodes.start) {
                this.buffer.appendChild(node);
            }
            while (this.tag.firstChild) {
                this.buffer.appendChild(this.tag.firstChild);
            }
            // Add persistent end nodes
            for (const node of this.persistentNodes.end) {
                this.buffer.appendChild(node);
            }
            resolve();
        });
    }



    /**
     * @public
     * @param {Data} data
     * @returns {Promise}
     */
    refresh(data) {
        this.data = data;
        return this.refreshAttributes(this.data)
            .then(() => this.refreshDescendantHandlers(data));
    }

    /**
     * @private
     * @param {Data} data
     * @returns {Promise}
     */
    refreshAttributes(data) {
        const attributePromises = this.attributeHandlers.map(h => h.refresh(data));
        return Promise.all(attributePromises);
    }

    /**
    * Rebuilds the body with persistent nodes and new content from the template.
    * @public
    * @param {DocumentFragment} bodyTemplate Contains the new content.
    */
    bind(bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
        // Add persistent start nodes
        for (const node of this.persistentNodes.start) {
            this.tag.appendChild(node);
        }
        while (bodyTemplate.firstChild) {
            this.tag.appendChild(bodyTemplate.firstChild);
        }
        for (const node of this.persistentNodes.end) {
            this.tag.appendChild(node);
        }
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
        // Move all dynamic children from the body tag back to the bodyTemplate. The body will be empty after this call.
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
        // Bind attributes to the body tag, creating AttributeHandlers for dynamic values
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
        // Removes all attributes from the body tag, except 'onload'.
        for (var name of this.tag.getAttributeNames()) {
            if (name == 'onload') {
                continue;
            }
            this.tag.removeAttribute(name);
        }
        this.attributeHandlers = [];
    }
}