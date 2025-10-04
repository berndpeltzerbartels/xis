// TagContentSetter.js
// Utility-Klasse zum Setzen von TagContent-Daten auf DOM-Elemente anhand id/tagName

/**
 * Setzt TagContent-Daten (z.B. für @TagContent-annotierte Felder) auf DOM-Elemente.
 * Erwartet zwei Objekte: idVariables und tagVariables (jeweils key: Datenobjekt)
 */
 class TagContentSetter {
    /**
     * Sucht die passenden Tags im Subtree und setzt die Daten.
     * @param {Element|Document} root - Root-Element (z.B. document oder ein Container)
     * @param {Object} idVariables - { id: {key: value, ...}, ... }
     * @param {Object} tagVariables - { tagName: {key: value, ...}, ... }
     */
    apply(root, idVariables, tagVariables) {
        this.applyIDVariables(root, idVariables);
        this.applyTagVariables(root, tagVariables);
    }

    applyIDVariables(root, idVariables) {
        if (idVariables && typeof idVariables === 'object') {
            var ids = Object.keys(idVariables);
            for (var i = 0; i < ids.length; i++) {
                var id = ids[i];
                var content = idVariables[id];
                var el = (root.getElementById ? root.getElementById(id) : document.getElementById(id));
                if (!el) {
                    throw new Error("TagContentSetter: Element mit id '" + id + "' nicht gefunden.");
                }
                this.setTagContent(el, content);
            }
        }
    }

    applyTagVariables(root, tagVariables) {
        if (tagVariables && typeof tagVariables === 'object') {
            var tagNames = Object.keys(tagVariables);
            for (var j = 0; j < tagNames.length; j++) {
                var tagName = tagNames[j];
                var content = tagVariables[tagName];
                var els = (root.getElementsByTagName ? root.getElementsByTagName(tagName) : document.getElementsByTagName(tagName));
                if (!els || els.length === 0) {
                    throw new Error("TagContentSetter: Kein Element für tagName '" + tagName + "' gefunden.");
                }
                for (var k = 0; k < els.length; k++) {
                    this.setTagContent(els[k], content);
                }
            }
        }
    }

    /**
     * Setzt den Inhalt auf das Tag
     * @param {Element} tagElement
     * @param {String|number} content
     */
    setTagContent(tagElement, content) {
        tagElement.innerText = content || '';
    }
}
