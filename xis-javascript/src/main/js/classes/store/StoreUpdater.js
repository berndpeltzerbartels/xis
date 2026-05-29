class StoreUpdater {
    updateStores(response) {
        this.storeLocalStorageData(response.localStorageData);
        this.storeSessionStorageData(response.sessionStorageData);
        this.storeClientStateData(response.clientStateData);
    }

    storeLocalStorageData(localStorageData) {
        if (!localStorageData) {
            return;
        }
        app.localStorage.saveData(localStorageData);
    }

    storeSessionStorageData(sessionStorageData) {
        if (!sessionStorageData) {
            return;
        }
        app.sessionStorage.saveData(sessionStorageData);
    }

    storeClientStateData(clientStateData) {
        if (!clientStateData) {
            return;
        }
        app.clientState.saveData(clientStateData);
    }

}
