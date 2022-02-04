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




var widget = {

    values : {'categories': categories},

    getValue: function(name) {
        return this.values[name];
    },

    show: function(container) {
        clearChildNodes(container);
        this.element = container;
        categoriesH3.show(this);
        // Children:
        categoriesFor.show(this);
    },
    getElement() {
        return this.element;
    }


}


var categoriesH3 = {
    show: function(parent) {
        this.parent = parent;
        this.element = appendElement(parent.getElement(), 'h3');
        categoriesText.show(this);
    },
    getValue: function(name) {
        return this.parent.getValue(name);
    },
    getElement() {
        return this.element;
    }
}


var categoriesText = {
    show: function(parent) {
        this.parent = parent;
        appendText(parent.getElement(), 'Kategorien')
    },
    getValue: function(name) {
        return this.parent.getValue(name);
    },
    getElement() {
        return this.parent.getElement();
    }
}

var categoriesFor = {
    names: ['i', 'categoryNumber','category'],
    show: function(parent) {
        this.parent = parent;
        this.variables = [];
        var array = parent.getValue('categories');
        for (var index = 0; index < array.length; index++) {
            this.variables['i'] = index;
            this.variables['categoryNumber'] = index + 1;
            this.variables['category'] = array[index];

            categoryDiv.show(this);
           

        }
    },
    getValue: function(name) {
        if (this.names.indexOf(name) != -1) { // may be, we want to return undefined !!!
            return this.variables[name];
        }
        return this.parent.getValue(name);
    },
    getElement() {
        return this.parent.getElement();
    }

}


var categoryDiv =  {
    show: function(parent) {
        this.parent = parent;
        this.element = appendElement(parent.getElement(), 'div');
        categoryH4.show(this);
        productsFor.show(this);
    },
    getValue: function(name) {
        return this.parent.getValue(name);
    },
    getElement() {
        return this.element;
    }
}


var categoryH4 = {
    show: function(parent) {
        this.parent = parent;
        this.element = appendElement(parent.getElement(), 'h4');
        categoryTitle.show(this);
    },
    getValue: function(name) {
        return this.parent.getValue(name);
    },
    getElement() {
        return this.element;
    }
}


var categoryTitle = {
    show: function(parent) {
        //${categoryNumber}. ${category.title}
        var e = parent.getElement();
        appendText(e, parent.getValue('categoryNumber'));
        appendText(e, '. ');
        appendText(e, parent.getValue('category').title); // ${category.title}
    },
    getValue: function(name) {
        return this.parent.getValue(name);
    },
    getElement() {
        return this.parent.getElement();
    }
}

var productsFor = {
    names: ['i', 'productNumber','product'],
    show: function(parent) {
        this.parent = parent;
        this.variables = [];
        var array = parent.getValue('category').products;
        for (var index = 0; index < array.length; index++) {
            this.variables['i'] = index;
            this.variables['productNumber'] = index + 1;
            this.variables['product'] = array[index];

            productDiv.show(this);

        }
    },
    getValue: function(name) {
        if (this.names.indexOf(name) != -1) { // may be, we want to return undefined !!!
            return this.variables[name];
        }
        return this.parent.getValue(name);
    },
    getElement() {
        return this.parent.getElement();
    }

}

var productDiv =  {
    show: function(parent) {
        this.parent = parent;
        this.element = appendElement(parent.getElement(), 'div');   
        productDetails.show(this);
    },
    getValue: function(name) {
        return this.parent.getValue(name);
    },
    getElement() {
        return this.element;
    }
}


// text-content
var productDetails = {
    show: function(parent) {
        var e = parent.getElement();
        appendText(e, parent.getValue('product').title);
        appendText(e, ' ');
        appendText(e, parent.getValue('product').price);
        appendText(e, ' EUR');
    },
    getValue: function(name) {
        return this.parent.getValue(name);
    },
    getElement() {
        return this.parent.getElement();
    }
}


// Util Functions

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

function clearChildNodes(parent) {

}

function appendElement(parent, tagName, attributes=[]) {
    var child = createElement(tagName, attributes);
    parent.appendChild(child);
    return child;
}

function appendText(parent, content) {
    if (!parent.innerText) {
        parent.innerText = content;
    } else {
        parent.innerText += content;
    }
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


function buttonClicked() {
    widget.show(byId('content'));
}
