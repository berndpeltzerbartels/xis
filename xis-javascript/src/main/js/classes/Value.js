class Value {

    constructor(element, serializerFunc) {
        this.element = element;
        this.serializerFunc = serializerFunc;
    }

    toJSON() {
        return this.toString();
    }

    toString() {
        return this.serializerFunc ? this.serializerFunc(this.element.value) : this.element.value;
    }
}