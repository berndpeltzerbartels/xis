function arrayContains(arr, value) {
    for (var i = 0; i < arr.length; i++) {
        if (arr[i] == value) return true;
    }
    return false;
}
