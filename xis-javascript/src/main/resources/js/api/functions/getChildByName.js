
/**
 * 
 * @param {Element} element 
 * @param {String} name 
 */
function getChildByName(element, name) {
     var nodeList = element.childNodes;
     for (var i = 0; i < nodeList.length; i++) {
        var child = nodeList.item(i);
        if (child.localName && child.localName == name) {
            return child;
        }
    }
}