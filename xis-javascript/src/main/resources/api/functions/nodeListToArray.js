function nodeListToArray(nodeList) {
    console.log('node-list:' + nodeList);
    console.log('node-list-length:' + nodeList.length);
    var arr = [];
    for (var i = 0; i < nodeList.length; i++) {
        arr.push(nodeList.item(i));
    }
    return arr;
}
