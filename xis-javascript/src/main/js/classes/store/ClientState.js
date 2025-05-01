class ClientState extends Store{
    constructor() {
        super();
        this.data = new Data({});
    }

    getValue(path) {
        return this.data.getValueByPath(path);
    }

    saveValue(path, value) {
        this.data.setValueByPath(path, value);
    }
}