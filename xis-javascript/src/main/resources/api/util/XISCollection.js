class XISCollection {

    /**
     * 
     * @param {Array} arr 
     */
    constructor(arr) {
        this.className = 'XISCollection';
        if (arr) {
            this.arr = arr;
        } else {
            this.arr = [];
        }
    }

    add(obj) {
        throw new Error('abstract method: add(obj)');
    }

    addAll(arr) {
        for (var e of arr) {
            this.add(e);
        }
    }

    contains(obj) {
        indexOf(this.arr, obj) != -1;
    }

    remove(obj) {
        var index = indexOf(this.arr, obj);
        if (index !== -1) {
            return this.arr.splice(index, 1);
        }
    }

    indexOf(obj) {
        return indexOf(this.arr, obj);
    }

    isEmpty() {
        return this.arr.length == 0;
    }

}