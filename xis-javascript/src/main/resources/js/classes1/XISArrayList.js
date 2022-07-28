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

    addAll(arr) {
        for (var e of arr) {
            this.arr.push(e);
        }
    }

    contains(obj) {
        this.arr.indexOf(obj) != -1;      
    }

    remove(obj) {
        var index = this.arr.indexOf(obj);
        if (index !== -1) {
            return this.arr.splice(index, 1);
        }
    }

    indexOf(obj) {
        return this.arr.indexOf(obj);
    }

    isEmpty() {
        return this.arr.length == 0;
    }

}