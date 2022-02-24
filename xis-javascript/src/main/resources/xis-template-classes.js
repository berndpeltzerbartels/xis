
function XISElement() {
    this.element = this.createElement();
    this.children = this.createChildren();
}

XISElement.prototype.init = function(parent, valueHolder) {
        this.parent = parent;
        this.valueHolder = valueHolder;
        this.parent.element.appendChild(this.element);
        for (var i = 0; i < this.children.length; i++) {
            this.children[i].init(this, valueHolder);
        }
    }

XISElement.prototype.getValue = function(path) {
        return this.valueHolder.getValue(path);
    }

 XISElement.prototype.update = function() {
        if (this.evalIf()) {
            this.updateAttributes();
            this.updateChildren();
        } else {
            this.unlink();
        }
    }

 XISElement.prototype.updateChildren = function() {
       for (var i = 0; i < this.children.length; i++) {
            this.children[i].update();
        }
    }

XISElement.prototype.updateAttributes = function() {
        // abstract
    }

XISElement.prototype.upateAttribute = function(name, value) {
        this.element.setAttribute(name, value);
    }

XISElement.prototype.evalIf = function() {
        return true;
    }


XISElement.prototype.createElement = function() {
        // abstract
    }

 XISElement.prototype.createChildren = function() {
        // abstract
        return [];
    }

 XISElement.prototype.unlink = function() {
        this.parent.removeChild(this.element);
    }


function XISMutableTextNode() {
        this.node = createTextNode();
    }

XISMutableTextNode.prototype.init = function(parent, valueHolder) {
        this.parent = parent;
        this.valueHolder = valueHolder;
        this.parent.element.appendChild(this.node);
    }

XISMutableTextNode.prototype.update = function() {
         var text = this.getText();
         if (this.node.nodeValue != text) {
             this.node.nodeValue = text;
         }
    }

XISMutableTextNode.prototype.getText = function() {
        // abstract. USE VALUE FIELD !!!
    }

XISMutableTextNode.prototype.getValue = function(name) {
        return this.valueHolder.getValue(name);
    }



function XISStaticTextNode() {
        this.node = createTextNode();
        this.node.nodeValue = this.getText();
    }

XISStaticTextNode.prototype.init = function(parent) {
        this.parent = parent;
        this.parent.element.appendChild(this.node);
    }

XISStaticTextNode.prototype.update = function() {
        // Nothing to to
    }

XISStaticTextNode.prototype.getText = function() {
        // abstract. USE VALUE FIELD !!!
    }




function XISLoopElement() {
        this.element = this.createElement();
        this.rows = [];
    }

XISLoopElement.prototype.getLoopAttributes = function() {
        // abstract
    }

XISLoopElement.prototype.init = function(parent, valueHolder) {
        loopAttributes = this.getLoopAttributes();
        this.parent = parent;
        this.valueHolder = valueHolder;
        this.parent.element.appendChild(this.element);
        this.names = [loopAttributes.itemVarName, loopAttributes.indexVarName, loopAttributes.numberVarName];
    }

XISLoopElement.prototype.createElement = function() {
        // abstract
    }

XISLoopElement.prototype.createChildren = function() {
        // abstract
        return [];
    }

XISLoopElement.prototype.unlink = function() {
        this.parent.removeChild(this.element);
    }

XISLoopElement.prototype.update = function() {
        if (this.evalIf()) {
            this.data = this.getArray();
            this.updateAttributes();
            this.updateAllChildren();
        } else {
            this.unlinkAll();
        }
    }


XISLoopElement.prototype.updateAllChildren = function() {
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

XISLoopElement.prototype.unlinkAll = function() {
        this.resize(0);
    }

XISLoopElement.prototype.updateAttributes = function() {
        // abstract
    }

XISLoopElement.prototype.upateAttribute = function(name, value) {
        this.element.setAttribute(name, value);
    }

XISLoopElement.prototype.getValue = function(path) {
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
        if (this.valueHolder) {
            return this.valueHolder.getValue(path);
        }
    }

XISLoopElement.prototype.getArray = function() {
        return this.parent.getValue(this.loopAttributes.arrayPath);
    }

XISLoopElement.prototype.resize = function(size) {
        while (this.rowCount() < size) {
            this.appendRow();
        }
        while (this.rowCount() > size) {
            this.removeRow();
        }
    }

XISLoopElement.prototype.rowCount = function() {
        return this.rows.length;
    }


XISLoopElement.prototype.appendRow = function() {
        var children = this.createChildren();
        for (var i = 0; i < children.length; i++) {
            children[i].init(this, this);
        }
        this.rows.push(children);
    }

XISLoopElement.prototype. removeRow = function() {
        if (this.rows.length > 0) {
            var children = this.rows.pop();
            for (var i = 0; i < children.length; i++) {
                this.element.removeChild(children[i].element);
            }
        }
    }

XISLoopElement.prototype.evalIf = function() {
        return true;
    }


function XISRoot() {
        this.element = this.createElement();
    }

XISRoot.prototype.init = function(parentElement) {
        this.parentElement = parentElement;
        this.parentElement.appendChild(this.element);
    }

XISRoot.prototype.update = function(clientState) {
        this.clientState = clientState;
        this.widget.update();
    }


XISRoot.prototype.getValue = function(path) {
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

XISRoot.prototype.createElement = function() {
        // abstract
    }

XISRoot.prototype.createChildren = function() {
        // abstract
        return [];
    }

function XISContainer() {
        this.element = this.createElement();
    }

XISContainer.prototype.init = function(parent, valueHolder) {
        this.parent = parent;
        this.valueHolder = valueHolder;
        this.parent.element.appendChild(this.element);
    }

XISContainer.prototype.setWidget = function(widgetName) {
        if (this.widget) {
            this.element.removeChild(this.widget.element);
        }
        this.widget = widgets.getWidgetByName(widgetName);
        this.widget.init(this, this.valueHolder);
        this.widget.update();
    }

XISContainer.prototype.getValue = function(path) {
        return this.valueHolder.getValue(path);
    }

XISContainer.prototype.update = function() {
        if (this.evalIf()) {
            this.updateAttributes();
            this.widget.update();

        } else {
            this.unlink();
        }
    }

XISContainer.prototype.updateAttributes = function() {
        // abstract
    }

XISContainer.prototype.upateAttribute = function(name, value) {
        this.element.setAttribute(name, value);
    }

XISContainer.prototype.evalIf = function() {
        return true;
    }


XISContainer.prototype.createElement = function() {
        // abstract
    }

XISContainer.prototype.unlink = function() {
        this.parent.removeChild(this.element);
    }

    XISContainer.prototype.getWidgets = function() {
            // abstract
        }



function XISWidgets() {
    this.widgets = [];
}

XISWidget.prototype.getWidgetByName = function(name) {
return this.widgets[name];
}
