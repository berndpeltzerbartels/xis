class XISTree {

    /**
     * @public
     */
    constructor() {
        this.className = 'XISState';
        this.data = {};
    }

    putValue(path, value) {
        var node = this.data;
        for (var i = 0; i < path.length - 1; i++) {
            var key = path[i];
            if (!node[key]) {
                node[key] = {}
            }
            node = node[key];
        }
        var leafKey = path[path.length - 1];
        node[leafKey] = value;
        var type = this.eventType;
        this.eventBus.fireEvent({
            type: type,
            path: path,
            value: value
        });
    }

    getValue(path) {
        var node = this.data;
        for (var i = 0; i < path.length - 1; i++) {
            var key = path[i];
            if (!node[key]) {
                return undefined;
            }
            node = node[key];
        }
        var leafKey = path[path.length - 1];
        return node[leafKey];
    }
}