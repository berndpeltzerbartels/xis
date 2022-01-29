var categories = [
    {title: 'Küche', products:[
        {title: 'Kochtopf', price: 12.5},
        {title: 'Braftpfanne', price: 12.5},
        {title: 'Topflappen', price: 12.5},
        {title: 'Espressokocher', price: 12.5}
    ]},
    {title: 'Bad', products:[
        {title: 'Vorleger', price: 12.5},
        {title: 'Klobürste', price: 12.5}
    ]}

];

function Variables() {
    this.values = [[]];
    this.level = 0;
}

Variables.prototype.levelUp = function() {
    this.level++;
    if (!this.values[this.level]) {
        this.values[this.level] = [];
    }
}


Variables.prototype.levelDown = function() {
    if (this.level > 0)
        this.level--;
}


Variables.prototype.setValue = function(varName, value) {
    this.values[this.level][varName] = value;
}

Variables.prototype.getValue = function(varName) {
    return this.values[this.level][varName];
}




function Widget() {

}

/**
 * 
 * @param {Element} parent 
 * @param {Number} level 
 * @param {String} variableName 
 * @param {function} funct 
 */
function IfControl(parent, level, variableName, funct = undefined ) {
    this.parent = parent;
    this.level = level;
    this.variableName = variableName;
    this.funct = funct;
    this.childControls = [];
    this.childElements = [];
    this.create();
}

IfControl.prototype.show = function() {
    nodeListToArray(element.childElements).forEach(e => showElement(e));
}


IfControl.prototype.hide = function() {
    nodeListToArray(element.childElements).forEach(e => hideElement(e));
}


IfControl.prototype.evaluate = function(contextVars) {
    var value = contextVars.getValue(this.variableName, this.level);
    if (this.funct) {
        value = this.funct(value);
    }
    if (value) {
        this.show();
    } else {
        this.hide();
    }
    this.childControls.forEach(control => control.evaluate(contextVars));
};


function byId(id) {
    return document.getElementById(id);
}


function hideElement(element) {
    console.log('hide');
    element.style = 'display: none';
}

function showElement(element) {
    element.style.display = 'display: block';
}

function createElement(tagName, attributes=[]) {
    var e = document.createElement(tagName);
    for (var name in attributes) {
       e.setAttribute(name, attributes[name]);
    }
    return e;
}

function nodeListToArray(nodeList) {
    var rv = [];
    for (var i = 0; i < nodeList.length; i++) {
        rv.push(nodeList.item(i));
    }
    return rv;
}

function notEmpty(value) {
    return value && value.length > 0;
}


var root = byId('list');
var ifCategories = new IfControl(root, 0, 'categories', notEmpty);

ifCategories.create = function() {

}
