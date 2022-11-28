class XISList extends XISCollection {

    /**
     * 
     * @param {Array} arr 
     */
    constructor(arr) {
        super(arr);
        this.className = 'XISList';
    }

    add(obj) {
        this.arr.push(obj);
    }
}