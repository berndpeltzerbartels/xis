class XISList {

    /**
     * 
     * @param {Array} arr 
     */
    constructor(arr) {
        if (arr) {
            this.arr = arr;
        } else {
            this.arr = [];
        }
    }

    add(obj) {
        this.arr.push(obj);
    }


    remove(obj) {
        var index = this.arr.indexOf(obj);
        if (index !== -1) {
            return this.arr.splice(index, 1);
        }
    }

    isEmpty() {
        return this.arr.length == 0;
    }

}