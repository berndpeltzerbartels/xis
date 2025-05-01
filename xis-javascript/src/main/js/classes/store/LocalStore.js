class LocalStore extends Store{
    constructor() {
        super();
    }

    getValue(path) {
        return localStorage.getItem(path);
    }

    saveValue(path, value) {
        localStorage.setItem(path, value);
    }
}