function indexOf(arr, value) {
    if (arr.indexOf) return arr.indexOf(value)
    for (var i = 0; i < arr.length; i++) {
        if (arr[i] == value) return true;
    }
    return false;
}
