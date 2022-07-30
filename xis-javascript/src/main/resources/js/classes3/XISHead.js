class XISHead extends XISElement {

    constructor(parent) {
        super(parent);
    }

    // TODO Das ist komplizierte. Uaser definded styles uns Scripts sollen hinzugefügt werden, während der Title nur aktualisiert werden darf.
    // TODO Kann ein Script vielleicht Variablen haben ? Ist das zu kompliziert ?

    init() {
        this.children.forEach(child => child.init());
    }


    refresh() {
        this.updateAttributes();
        this.children.forEach(child => child.refresh());
    }
}