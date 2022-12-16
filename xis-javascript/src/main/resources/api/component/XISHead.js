class XISHead extends XISElement {

    constructor(parentPage) {
        super(parentPage);
        this.className = 'XISHead';
    }


    createElement() {
        return createElement('head'); // dummy to keep child-nodes
    }

    /**
     * @public
     * @override
     */
    getElement() {
        return this.element;
    }

    // TODO Das ist komplizierte. User definded styles uns Scripts sollen hinzugefügt werden, während der Title nur aktualisiert werden darf.
    // TODO Kann ein Script vielleicht Variablen haben ? Ist das zu kompliziert ?

    /**
    * @public
    * @override
    */
    init() {
        this.title = this.children.find(child => child.element.localName == 'title');
        this.children.array.forEach(child => child.init());
    }

    /**
     * @public
     * @override
     */
    show() {
        super.show();
        if (this.title) {
            this.title.show();
        }
    }

    /**
     * @public
     * @override
     */
    hide() {
        super.hide();
        if (this.title) {
            this.title.hide();
        }
    }

    /**
     * @public
     * @override
     */
    destroy() {
        super.destroy();
        if (this.title) {
            this.title.destroy();
        }
    }

    refresh() {
        super.refresh();
        if (this.title) {
            this.title.refresh();
        }
    }
}