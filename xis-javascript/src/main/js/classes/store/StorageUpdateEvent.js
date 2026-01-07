class StorageUpdateEvent {

    /**
     * Creates an instance of StoreUpdateEvent.
     * @param {string} key - The key of the updated item.
     * @param {any} value - The new value of the updated item.
     * @param {boolean} doDelete - Whether the item was deleted.
     */
    constructor( key, value, doDelete) {
        this.key = key;
        this.value = value;
        this.doDelete = doDelete;
        this.timestamp = new Date();
    }

  }