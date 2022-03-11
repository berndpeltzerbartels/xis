function XISElement() { }

XISElement.prototype.init = function (parent, valueHolder) {
    this.parent = parent;
    this.valueHolder = valueHolder;
    this.parent.appendChild(this.element);
    this.initChildren();
}

XISElement.prototype.getValue = function (path) {
    return this.valueHolder.getValue(path);
}

XISElement.prototype.update = function () {
    this.updateAttributes();
    this.updateChildren();
}

XISElement.prototype.updateChildren = function () {
    for (var i = 0; i < this.children.length; i++) {
        this.children[i].update();
    }
}

XISElement.prototype.initChildren = function () {
    for (var i = 0; i < this.children.length; i++) {
        this.children[i].init(this.element, this.valueHolder);
    }
}

XISElement.prototype.updateAttributes = function () {
    // abstract
}

XISElement.prototype.updateAttribute = function (name, value) {
    this.element.setAttribute(name, value);
}

XISElement.prototype.unlink = function () {
    this.parent.removeChild(this.element);
}


function XISMutableTextNode() {
    this.node = createTextNode('');
}

XISMutableTextNode.prototype.init = function (parent, valueHolder) {
    this.parent = parent;
    this.valueHolder = valueHolder;
    this.parent.appendChild(this.node);
}

XISMutableTextNode.prototype.update = function () {
    var text = this.getText();
    if (this.node.nodeValue != text) {
        this.node.nodeValue = text;
    }
}

XISMutableTextNode.prototype.getText = function () {
    // abstract. USE VALUE FIELD !!!
}

XISMutableTextNode.prototype.getValue = function (name) {
    return this.valueHolder.getValue(name);
}



function XISStaticTextNode() { }

XISStaticTextNode.prototype.init = function (parent) {
    this.parent = parent;
    this.parent.appendChild(this.node);
}

XISStaticTextNode.prototype.update = function () {
    // Nothing to to
}



function XISIf() { }

XISIf.prototype.init = function (parent, valueHolder) {
    this.parent = parent;
    this.valueHolder = valueHolder;
}

XISIf.prototype.update = function () {
    if (this.evaluateCondition()) {
        this.initChildren();
    } else {
        this.unlinkChildren();
    }
    this.updateAttributes();
    this.updateAllChildren();
}

XISIf.prototype.evaluateCondition = function () {
    return this.getValue(this.path); // TODO evaluate any expression
}

XISIf.prototype.initChildren = function() {
    for (var i = 0; i < this.children.length; i++) {
        this.children[i].init(this.parent, this.valueHolder);
    }
}

XISIf.prototype.unlinkChildren = function() {
    for (var i = 0; i < this.children.length; i++) {
        this.children[i].unlink();
    }
}

XISIf.prototype.getValue = function(path) {
    return this.valueHolder.getValue(path);
}


function XISLoop() { }

XISLoop.prototype.init = function (parent, valueHolder) {
    this.parent = parent;
    this.valueHolder = valueHolder;
    this.names = [this.loopAttributes.itemVarName, this.loopAttributes.indexVarName, this.loopAttributes.numberVarName];
}

XISLoop.prototype.update = function () {
    this.data = this.getArray();
    this.updateChildren();
}

XISLoop.prototype.updateChildren = function () {
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

XISLoop.prototype.getValue = function (path) {
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

XISLoop.prototype.getArray = function () {
    return this.valueHolder.getValue(this.loopAttributes.arrayPath);
}

XISLoop.prototype.resize = function (size) {
    while (this.rowCount() < size) {
        this.appendRow();
    }
    while (this.rowCount() > size) {
        this.removeRow();
    }
}

XISLoop.prototype.rowCount = function () {
    return this.rows.length;
}


XISLoop.prototype.appendRow = function () {
    var children = this.createChildren();
    for (var i = 0; i < children.length; i++) {
        children[i].init(this.parent, this);
    }
    this.rows.push(children);
}

XISLoop.prototype.removeRow = function () {
    if (this.rows.length > 0) {
        var children = this.rows.pop();
        for (var i = 0; i < children.length; i++) {
            this.element.removeChild(children[i].element);
        }
    }
}

function XISWidget() { }

XISWidget.prototype.init = function (parentElement, valueHolder) {
    this.parentElement = parentElement;
    this.root.init(this.parentElement, valueHolder);
}

XISWidget.prototype.update = function (data) {
    this.data = data;
    this.root.update();
}

function XISPage() { }

XISPage.prototype.init = function () {
    this.head.element = document.getElementsByTagName('head').item(0);
    this.body.element = document.getElementsByTagName('body').item(0);
    this.head.valueHolder = this;
    this.body.valueHolder = this;
    this.head.unlink = function() {}
    this.body.unlink = function() {}
    this.initChildren();
}

XISPage.prototype.initChildren = function () {
    this.head.initChildren();
    this.body.initChildren();
}

XISPage.prototype.updateChildren = function () {
    this.head.update();
    this.body.update();
}

XISPage.prototype.update = function (data) {
    this.data = data;
    this.updateChildren();
}

XISPage.prototype.getValue = function (path) {
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


function XISContainer() { }

XISContainer.prototype.init = function (parent, valueHolder) {
    this.parent = parent;
    this.valueHolder = valueHolder;
    this.parent.appendChild(this.element);
    if (this.defaultWidgetId) {
        this.setWidget(this.defaultWidgetId);
    }
}

XISContainer.prototype.setWidget = function (widgetId) {
    if (this.widget) {
        this.element.removeChild(this.widget.element);
    }
    this.widget = widgets.getWidget(widgetId);
    this.widget.init(this.element, this.valueHolder);
}

XISContainer.prototype.getValue = function (path) {
    return this.valueHolder.getValue(path);
}

XISContainer.prototype.update = function () {
    this.updateAttributes();
    if (this.widget) {
        this.widget.update();
    }
}

XISContainer.prototype.updateAttributes = function () {
    // abstract
}

XISContainer.prototype.updateAttribute = function (name, value) {
    this.element.setAttribute(name, value);
}

XISContainer.prototype.unlink = function () {
    this.parent.removeChild(this.element);
}

XISContainer.prototype.getWidgets = function () {
    // abstract
}


function XISWidgets() {
    this.widgets = [];
    this.widgetsByPath = {};
}

XISWidgets.prototype.getWidget = function (widgetId) {
    return this.widgets[widgetId];
}

XISWidgets.prototype.getWidgetByPath = function (path) {
    for (var i = 0; i < this.widgets.length; i++) {
        var widget = this.widgets[i];
        if (widget.path && widget.path == path) {
            return widget;
        }
    }
}

XISWidgets.prototype.bind = function (widgetId, element) {
    var widget = this.getWidget(widgetId);
    widget.init(element);
    return widget;
}



function XISPages() {}


XISPages.prototype.bind = function(path) {
    var root = document.getRootNode();
    var pageWidget = this.getPageWidgetByPath(path);

}

XISPages.prototype.getPageWidgetByPath = function(path) {
    return this.pageWidgets[path];
}

XISPages.prototype.getData = function() {

}

/*

    this.element = document.getElementsByTagName('html').item(0);
    this.path = window.location.pathname;
    this.widget = widgets.getWidgetByPath(httpPath);
    this.widget.init(this.element);
function transformToAssocArray( prmstr ) {
    var params = {};
    var prmarr = prmstr.split("&");
    for ( var i = 0; i < prmarr.length; i++) {
        var tmparr = prmarr[i].split("=");
        params[tmparr[0]] = tmparr[1];
    }
    return params;
}
*/