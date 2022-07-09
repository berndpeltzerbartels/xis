class XISBody extends XISTemplateObject {

    constructor() {
        this.children = this.createChildren();
    }

    onDataChanged() {
        this.children.forEach(child => child.onDataChanged());
    }    
    
    
}