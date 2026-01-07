class StoreUpdater {
    updateStores(response) {
        this.storeLocalStorageData(response.localStorageData);
        this.storeSessionStorageData(response.sessionStorageData);
        this.storeClientStorageData(response.clientStorageData);
        this.storeLocalDatabaseData(response.localDatabaseData);
    }

    storeLocalStorageData(localStorageData) {
        if (!localStorageData) {
            return;
        }
        app.localStorage.saveData(localStorageData);
    }

    storeSessionStorageData(sessionStorageData) {
        app.sessionStorage.saveData(sessionStorageData);
    }

    storeClientStorageData(clientStorageData) {
        if (!clientStorageData) {
            return;
        }
        app.clientStorage.saveData(clientStorageData);
    }


    storeLocalDatabaseData(localDatabaseData) {
        if (!localDatabaseData) {
            return;
        }
        // TODO create and configure db
        for (var key of Object.keys(localDatabaseData)) {
            this.localDatabase.setItem(key, localDatabaseData[key]);
        }
    }
}