function getElementByTagName(name) {
    console.log('name:' + name);
    var arr = nodeListToArray(document.getElementsByTagName(name));
    return arr.length > 0 ? arr[0] : undefined;
}