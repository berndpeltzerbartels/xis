/**
 * Utility-class to manipulate the document-tree.
 */
class DomAccessor {

/**
 * Surrounds the given element with the given elementToInsert. Is equivalent to
 * inserting elementToInsert as parent of element.

 * @public
 * @param {Element} element
 * @param {Element} elementToInsert
 */
    surroundWith(element, elementToInsert) {
        this.insertParent(element, elementToInsert);
    }

    /**
     * @public
     * @param {Element} element 
     * @param {Element} elementToInsert 
     */
    insertParent(element, elementToInsert) {
        if (element.parentNode) {
            this.replaceElement(element, elementToInsert);
            elementToInsert.appendChild(element);
        } else {
            elementToInsert.appendChild(element);
        }
    }

    /**
     * @public
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

    /**
     * @public
     * @param {Element} old 
     * @param {Element} replacement 
     */
    replaceElement(old, replacement) {
        var parent = old.parentNode;
        if (parent) {
            var nextSibling = old.nextSibling;
            parent.removeChild(old);
            if (nextSibling) {
                parent.insertBefore(replacement, nextSibling);
            } else {
                parent.appendChild(replacement);
            }
            // TODO move child nodes ?
        }
    }
}