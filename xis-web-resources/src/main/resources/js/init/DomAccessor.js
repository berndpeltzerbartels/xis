
class DomAccessor {

    insertParent(element, elementToInsert) {
        console.log('insertParent: ' + element + ", " + elementToInsert);
        if (element.parentNode) {
            console.log(element + ' has parent');
            this.replaceElement(element, elementToInsert);
            elementToInsert.appendChild(element);
        } else {
            console.log(element + ' has no parent');
            element.parentNode = elementToInsert;
            elementToInsert.appendChild(element);
        }
    }

    /**
     * 
     * @param {Element} element 
     * @param {Element} elementToInsert 
     */
    insertChild(element, elementToInsert) {
        var childArray = nodeListToArray(element.childNodes);
        for (var child of childArray) {
            element.removeChild(child);
            elementToInsert.appendChild(child);
        }
        element.appendChild(elementToInsert);
    }

    replaceElement(old, replacement) {
        var parent = old.parentNode;
        var nextSibling = old.nextSibling;
        parent.removeChild(old);
        if (nextSibling) {
            parent.insertBefore(replacement, nextSibling);
        } else {
            parent.appendChild(replacement);
        }

    }
}