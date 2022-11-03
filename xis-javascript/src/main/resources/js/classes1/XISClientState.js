class XISClientState {

    constructor() {
        this.state = {};
    }

    putValue(key, value) {
        this.state[key] = value;
    }

    getValue(key) {
        return this.state[key];
    }

    processData(data) {
        for (key in Object.keys(data)) {
            this.putValue(key, data[key]);
        }
    }
}