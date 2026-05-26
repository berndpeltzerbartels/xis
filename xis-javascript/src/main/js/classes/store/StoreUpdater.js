class StoreUpdater {
    updateStores(response) {
        this.storeLocalStorageData(response.localStorageData);
        this.storeSessionStorageData(response.sessionStorageData);
        this.storeClientStateData(response.clientStateData);
        this.storeLocalDatabaseData(response.localDatabaseData);
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
