class XISHead extends XISElement {

    bind(parent) {
        // noop
    }

    // TODO Das ist komplizierte. Uaser definded styles uns Scripts sollen hinzugefügt werden, während der Title nur aktualisiert werden darf.
    // TODO Kann ein Script vielleicht Variablen haben ? Ist das zu kompliziert ?

    refresh() {
        this.updateAttributes();
        this.children.forEach(child => child.refresh());
    }
}