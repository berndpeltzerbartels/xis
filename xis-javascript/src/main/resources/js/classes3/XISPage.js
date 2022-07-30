class XISPage extends XISValueHolder {

    constructor() {
        super(undefined);
        this.headChildNodes = [];
    }


    init() {
        this.head.init();
        this.body.init();
    }


    refresh(rootHead, rootBody) {
        this.head.refresh();
        this.body.refresh();
        this.removeBodyAttributes(rootBody);
        this.unbindHeadContent(rootHead);
        this.unbindBodyContent(rootBody);
        this.setBodyAttributes(rootBody);
        this.bindHeadContent(rootHead);
        this.bindBodyContent(rootBody);
    }


    /**
     * We do not remove all childnodes but only those ones from this page.
     * Otherwise we would remove all script-tags.
     * 
     * @param {Element} head 
     */
    unbindHeadContent(head) {
        debugger;
        while (this.headChildNodes.length > 0) {
            var child = this.headChildNodes.pop();
            head.removeChild(child);
        }
    }

    /**
     * here we cann remove all, because body is empty on root-page.
    * @param {Element} body 
    */
    unbindBodyContent(body) {
        var nodeList = body.childNodes;
        for (var i = 0; i < nodeList.length; i++) {
            body.removeChild(nodeList.item(i));
        }
    }

    /**
     * @param {Element} head 
     */
    bindHeadContent(head) {
        debugger;
        var nodeList = this.head.element.childNodes;
        for (var i = 0; i < nodeList.length; i++) {
            this.headChildNodes.push(head.appendChild(nodeList.item(i)));
        }
    }

     /**
     * Used in RootPage
     * @param {Element} body 
     */
    bindBodyContent(body) {
        debugger;
        var nodeList = this.body.element.childNodes;
        for (var i = 0; i < nodeList.length; i++) {
            body.appendChild(nodeList.item(i));
        }
    }

    /**
     * Set html-attributes of this page's body to given body-tag.
     * @param {Element} bodyTag 
     */
    setBodyAttributes(bodyTag) {
       for (var name of this.body.element.getAttributeNames()) {
           var value = this.body.element.getAttribute(name);
           bodyTag.setAttribute(name, value);
       }
     }
 
    /**
     * Remove html-attributes of this page's body from given body-tag.
     * @param {Element} bodyTag 
     */
     removeBodyAttributes(bodyTag) {
        for (var name of this.body.element.getAttributeNames()) {
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
    
    createChildren() {
       this.createHeadElement();
       this.createBodyElement();
    }
*/


}