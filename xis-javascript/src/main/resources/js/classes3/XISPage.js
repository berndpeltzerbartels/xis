class XISPage extends XISValueHolder {

    /**
     * Used in RootPage
     * @param {Element} head 
     */
    unbindHeadContent(head) {
        var nodeList = this.head.childNodes();
        for (var i = 0; i < nodeList.length; i++) {
            head.removeChild(nodeList.item(i));
        }
    }

    /**
    * Used in RootPage
    * @param {Element} body 
    */
    unbindBodyContent(body) {
        var nodeList = this.body.childNodes();
        for (var i = 0; i < nodeList.length; i++) {
            body.removeChild(nodeList.item(i));
        }
    }

    /**
     * Used in RootPage
     * @param {Element} head 
     */
    bindHeadContent(head) {
        var nodeList = this.head.childNodes();
        for (var i = 0; i < nodeList.length; i++) {
            head.appendChild(nodeList.item(i));
        }
    }

     /**
     * Used in RootPage
     * @param {Element} body 
     */
    bindBodyContent(body) {
        var nodeList = this.body.childNodes();
        for (var i = 0; i < nodeList.length; i++) {
            body.appendChild(nodeList.item(i));
        }
    }

    /**
     * Set html-attributes of this page's body to given body-tag.
     * @param {Element} bodyTag 
     */
    setBodyAttributes(bodyTag) {
       for (var name of this.body.getAttributeNames()) {
           var value = this.body.getAttribute(name);
           bodyTag.setAttribute(name, value);
       }
     }
 
    /**
     * Remove html-attributes of this page's body from given body-tag.
     * @param {Element} bodyTag 
     */
     removeBodyAttributes(bodyTag) {
        for (var name of this.body.getAttributeNames()) {
            bodyTag.removeAttribute(name);
        }
     }

     /**
      * @param {any} data 
      */
     processData(data) {
        this.setValues(data);
        this.refresh();
     }


    /**
    * Creates Childclasses, not Elements.
    */
    createChildren() {
       this.createHeadElement();
       this.createBodyElement();
    }

    /**
    * Called when data changed. Tree is reloaded.
    */
    refresh() {
        this.head.refresh();
        this.body.refresh();
    }

}