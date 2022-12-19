function arrayToString(arr) {
    var str = '';
    if (!arr) {
        return 'null';
    }
    str += '[';
    for (var i = 0; i < arr.length; i++) {
        str += arr[i];
        if (i < arr.length - 1) {
        str += ',';}

    }
    str += ']';
    return str;
}