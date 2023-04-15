
class DomAccessor {

    insertParent(element, elementToInsert) {
        this.replaceElement(element, elementToInsert);
        elementToInsert.appendChild(element);
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