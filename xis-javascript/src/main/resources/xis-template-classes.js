

/************************** LoopAttributes ********************************************/

class LoopAttributes {
    arrayPath = [];
    itemVarName;
    indexVarName;
    numberVarName;
}

/************************** XISElement ********************************************/

class XISElement {

    constructor() {
        this.element = this.createElement();
        this.children = this.createChildren();
    }

    init(parent) {
        this.parent = parent;
        this.parent.element.appendChild(this.element);
        for (var child of this.children) {
            child.init(this);
        }
    }

    getValue(path) {
        return this.parent.getValue(path);
    }

    update() {
        if (this.evalIf()) {
            this.updateAttributes();
            this.updateChildren();
        } else {
            this.unlink();
        }
    }

    updateChildren() {
        for (var child of this.children) {
            child.update();
        }
    }

    updateAttributes() {
        // abstract
        /*
        var attr = '';

        attr = '';
        attr += this.parent.getValue('');
        if (attr != this.element.getAttribute('style')) {
            this.element.setAttribute('style', attr);
        }

        ...
        
        this.element.setAttribute()
        */
    }

    evalIf() {
        return true;
    }


    createElement() {
        // abstract
    }

    createChildren() {
        // abstract
        return [];
    }

    unlink() {
        this.parent.removeChild(this.element);
    }
}


class XISMutableTextNode {

    constructor() {
        this.node = createTextNode();
    }

    init(parent) {
        this.parent = parent;
        this.parent.element.appendChild(this.node);
    }

    update() {
         var text = this.getText();
         if (this.node.nodeValue != text) {
             this.node.nodeValue = text;
         }
    }

    getText() {
        // abstract. USE VALUE FIELD !!!
    }
}


class XISStaticTextNode {

    constructor() {
        this.node = createTextNode();
        this.node.nodeValue = this.getText();
    }

    init(parent) {
        this.parent = parent;
        this.parent.element.appendChild(this.node);
    }

    update() {
        // Nothing to to
    }

    getText() {
        // abstract. USE VALUE FIELD !!!
    }
}



class XISLoopElement {

    constructor() {
        this.element = this.createElement();
        this.rows = [];
    }

    getLoopAttributes() {
        // abstract
    }

    init(parent) {
        loopAttributes = this.getLoopAttributes();
        this.parent = parent;
        this.parent.element.appendChild(this.element);
        this.names = [loopAttributes.itemVarName, loopAttributes.indexVarName, loopAttributes.numberVarName];
    }

    createElement() {
        // abstract
    }

    createChildren() {
        // abstract
        return [];
    }

    unlink() {
        this.parent.removeChild(this.element);
    }

    update() {
        if (this.evalIf()) {
            this.data = this.getArray();
            this.updateAttributes();
            this.updateAllChildren();
        } else {
            this.unlinkAll();
        }
    }


    updateAllChildren() {
        this.data = [];
        this.values = [];
        this.resize(this.data.length);
        for (var i = 0; i < this.data.length; i++) {
            this.values = [];
            this.values[this.loopAttributes.itemVarName] = this.data[i];
            this.values[this.loopAttributes.indexVarName] = i;
            this.values[this.loopAttributes.numberVarName] = i + 1;
            var children = this.rows[i];
            for (var j = 0; j < children.length; j++) {
                children[j].update();
            }
        }
    }

    unlinkAll() {
        this.resize(0);
    }

    updateAttributes() {
        // abstract
        var attr = '';

        attr = '';
        attr += this.getValue(''); // NOT PARENT, HERE !!!!
        if (attr != this.element.getAttribute('style')) {
            this.element.setAttribute('style', attr);
        }

        // etc
        // this.element.setAttribute()
    }

    getValue(path) {
        var name = path[0];
        if (this.names.indexOf(name) != -1) {
            var rv = this.values[name];
            for (var i = 1; i < path.length; i++) {
                if (!rv) {
                    return undefined;
                }
                rv = rv[path[i]];
            }
            return rv;
        }
        if (this.parent) {
            return this.parent.getValue(path);
        }
    }

    getArray() {
        return this.parent.getValue(this.loopAttributes.arrayPath);
    }

    resize(size) {
        while (this.rowCount() < size) {
            this.appendRow();
        }
        while (this.rowCount() > size) {
            this.removeRow();
        }
    }

    rowCount() {
        return this.rows.length;
    }


    appendRow() {
        var children = this.createChildren();
        for (var i = 0; i < children.length; i++) {
            children[i].init(this);
        }
        this.rows.push(children);
    }

    removeRow() {
        if (this.rows.length > 0) {
            var children = this.rows.pop();
            for (var i = 0; i < children.length; i++) {
                this.element.removeChild(children[i].element);
            }
        }
    }

    evalIf() {
        return true;
    }
}

class XISRoot {

    constructor() {
        this.element = this.createElement();
    }

    init(parentElement) {
        this.parentElement = parentElement;
        this.parentElement.appendChild(this.element);
    }

    update(clientState) {
        this.clientState = clientState;
        this.widget.update();
    }


    getValue(path) {
        var name = path[0];
        var rv = this.clientState[name];
        for (var i = 1; i < path.length; i++) {
            if (!rv) {
                return undefined;
            }
            rv = rv[path[i]];
        }
        return rv;
    }

    createElement() {
        // abstract
    }

}

class XISContainer {

    constructor() {
        this.element = this.createElement();
    }

    init(parent) {
        this.parent = parent;
        this.parent.element.appendChild(this.element);
    }

    setWidget(widget) {
        if (this.widget) {
            this.element.removeChild(this.widget.element);
        }
        this.widget = widget;
        this.widget.init(this);
        this.widget.update();
    }

    getValue(path) {
        return this.parent.getValue(path);
    }

    update() {
        if (this.evalIf()) {
            this.updateAttributes();
            this.widget.update();

        } else {
            this.unlink();
        }
    }

    updateAttributes() {
        // abstract
        /*
        var attr = '';

        attr = '';
        attr += this.parent.getValue('');
        if (attr != this.element.getAttribute('style')) {
            this.element.setAttribute('style', attr);
        }

        ...
        
        this.element.setAttribute()
        */
    }

    evalIf() {
        return true;
    }


    createElement() {
        // abstract
    }

    unlink() {
        this.parent.removeChild(this.element);
    }
}

