class XISMap {

    constructor() {
        this.className = 'XISMap';
        this.data = {};
    }

    put(key, value) {
        this.data[key] = value;
        return value;
    }

    putIfAbsent(key, value) {
        if (!this.data[key]) {
            this.data[key] = value;
        }
        return this.data[key];
    }

    putAll(data) {
        if (data.className == this.className) {
            for (key in data.keys()) {
                this.put(key, data.get(key));
            }
        } else {
            for (var key in Object.keys(data)) {
                this.put(key, data[key]);
            }
        }
    }

    get(key) {
        return this.data[key];
    }

    keys() {
        return Object.keys(this.data);
    }

    containsKey(key) {
        return this.data[key] !== undefined;
    }

}