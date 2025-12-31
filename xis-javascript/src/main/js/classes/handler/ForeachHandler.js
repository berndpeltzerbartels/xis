class ForeachHandler extends TagHandler {

    /**
     * @param {Element} tag the custom tag '<xis:foreach/>'
     * @param {TagHandler} tagHandlers
     */
    constructor(tag, tagHandlers) {
        super(tag);
        this.tagHandlers = tagHandlers;
        this.arrayPathExpression = this.createExpression(this.variableToKey(this.getAttribute('array')), '.');
        this.varName = this.getAttribute('var');
        this.type = 'foreach-handler';
        this.priority = 'high';
        this.cache = new ForEachNodeCache(nodeListToArray(this.tag.childNodes));
        this.clearChildren();
        this.parentElement = this.tag.parentNode;
        this.siblingBehind = this.tag.nextSibling;
    }

    /**
     * @public
     * @param {Data} data
     */
    refresh(data) {
        var arrayPath = this.doSplit(this.arrayPathExpression.evaluate(data), '.');
        var arr = data.getValue(arrayPath);

        // Handle indirect array references: if arr is a string, resolve it as a path
        // Example: array="${arrayName}" where arrayName="list1" -> resolve to data.list1
        if (typeof arr === 'string') {
            var resolvedPath = this.doSplit(arr, '.');
            arr = data.getValue(resolvedPath);
        }

        if (!arr) {
          return;
        }
        this.cache.sizeUp(arr.length);
        for (var i = 0; i < this.cache.length; i++) {
            var subData = new Data({}, data);
            this.setValidationPath(subData, this.varName, i);
            subData.setValue([this.varName + 'Index'], i);
            subData.setValue([this.varName], arr[i]);
            var children = this.cache.getChildren(i);
            if (i < arr.length) {
                for (var child of children) {
                    if (child.parentNode != this.tag) {
                        this.tag.appendChild(child);
                    }
                    const childHandler = this.tagHandlers.getRootHandler(child);
                    childHandler.parentHandler = this;
                    this.descendantHandlers.push(childHandler);
                    childHandler.refresh(subData);
                }
            } else {
                // Cache is too long. We remove unused elements
                for (var child of children) {
                    if (child.parentNode == this.tag) {
                        this.tag.removeChild(child);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    /**
     * State-aware refresh: rebuilds iteration but children get normal refresh
     * since transformed values (item) are no longer state variables.
     */
    stateRefresh(data, invoker) {
        if (this === invoker) {
            return;
        }
        // Rebuild iteration with new state data
        this.refresh(data);
        // Note: child handlers already refreshed in refresh() method with transformed data
    }

    setValidationPath(subData, varName, index) {
        if (!subData.validationPath) {
            return; // we are not inside a form
        }
        subData.validationPath += '/' + varName + '[' + index + ']';
    }

    /**
    * if variable starts with '${' and ends with '}', it is an expression.
    * This method removes ${ and }, because here we do not deal with a textual expression,
    * but with a variable name used in data binding.
    * @param {string} variable
    * @returns {string} the key for data binding
    */
    variableToKey(variable) {
       if (variable.startsWith('${') && variable.endsWith('}')) {
           return variable.slice(2, -1);
       }
       return variable;
    }
}