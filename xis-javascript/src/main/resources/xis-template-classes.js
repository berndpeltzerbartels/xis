
function XISElement() {}

XISElement.prototype.init = function(parent, valueHolder) {
        this.parent = parent;
        this.valueHolder = valueHolder;
        this.parent.element.appendChild(this.element);
        this.initChildren();
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
 XISElement.prototype.initChildren = function() {
       for (var i = 0; i < this.children.length; i++) {
            this.children[i].init(this, this.valueHolder);
        }
    }
XISElement.prototype.updateAttributes = function() {
        // abstract
    }

XISElement.prototype.updateAttribute = function(name, value) {
        this.element.setAttribute(name, value);
    }

XISElement.prototype.evalIf = function() {
        return true;
    }

 XISElement.prototype.unlink = function() {
        this.parent.removeChild(this.element);
    }


function XISMutableTextNode() {
        this.node = createTextNode('');
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



function XISStaticTextNode() {}

XISStaticTextNode.prototype.init = function(parent) {
        this.parent = parent;
        this.parent.element.appendChild(this.node);
    }

XISStaticTextNode.prototype.update = function() {
        // Nothing to to
    }

function XISLoopElement() {}

XISLoopElement.prototype.init = function(parent, valueHolder) {
        this.parent = parent;
        this.valueHolder = valueHolder;
        this.parent.element.appendChild(this.element);
        this.names = [this.loopAttributes.itemVarName, this.loopAttributes.indexVarName, this.loopAttributes.numberVarName];
    }

XISLoopElement.prototype.unlink = function() {
        this.parent.removeChild(this.element);
    }

XISLoopElement.prototype.update = function() {
        this.data = [];
        if (this.evalIf()) {
            this.data = this.getArray();
            this.updateAttributes();
            this.updateAllChildren();
        } else {
            this.unlinkAll();
        }
    }


XISLoopElement.prototype.updateAllChildren = function() {
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

XISLoopElement.prototype.updateAttribute = function(name, value) {
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
        return this.valueHolder.getValue(this.loopAttributes.arrayPath);
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


function XISRoot() {}

XISRoot.prototype.init = function(parentElement) {
        this.parentElement = parentElement;
        this.parentElement.appendChild(this.element);
        this.initChildren();
    }

XISRoot.prototype.update = function(data) {
    this.data = data;
    this.updateChildren();
}

XISRoot.prototype.updateChildren = function() {
  for (var i = 0; i < this.children.length; i++) {
       this.children[i].update();
   }
}

 XISRoot.prototype.initChildren = function() {
       for (var i = 0; i < this.children.length; i++) {
            this.children[i].init(this, this);
        }
    }


XISRoot.prototype.getValue = function(path) {
        var name = path[0];
        var rv = this.data[name];
        for (var i = 1; i < path.length; i++) {
            if (!rv) {
                return undefined;
            }
            rv = rv[path[i]];
        }
        return rv;
    }

function XISContainer() {}

XISContainer.prototype.init = function(parent, valueHolder) {
        this.parent = parent;
        this.valueHolder = valueHolder;
        this.parent.element.appendChild(this.element);
        if (this.defaultWidgetId) {
            this.setWidget(this.defaultWidgetId);
        }
    }

XISContainer.prototype.setWidget = function(widgetId) {
        if (this.widget) {
            this.element.removeChild(this.widget.element);
        }
        this.widget = widgets.getWidget(widgetId);
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

XISContainer.prototype.updateAttribute = function(name, value) {
        this.element.setAttribute(name, value);
    }

XISContainer.prototype.evalIf = function() {
        return true;
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

XISWidgets.prototype.getWidget = function(widgetId) {
return this.widgets[widgetId];
}


XISWidgets.prototype.bind = function(widgetId, element) {
    var widget = this.getWidget(widgetId);// XISRoot
    widget.init(element);
    return widget;
}
