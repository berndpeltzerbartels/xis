
class DomAccessor {

    insertParent(element, elementToInsert) {
       this.replaceElement(element, elementToInsert);
       elementToInsert.appendChild(element);
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

module.exports = {
	DomAccessor: DomAccessor
};
