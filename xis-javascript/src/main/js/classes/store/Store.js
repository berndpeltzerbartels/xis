class Store {
    /*
        * @param {EventPublisher} eventPublisher
        * @param {Storage} storageArea
        * @param {string} eventKey
        */
    constructor( eventPublisher, storageArea, eventKey) {
        this.eventPublisher = eventPublisher;
        this.storageArea = storageArea;
        this.eventKey = eventKey;
        this.pathMap = {};
        window.addEventListener('storage', (event) => this.onEvent(event));
    }


    getValue(path) {
        if (path === undefined || path.length === 0) {
            throw new Error('Path is undefined or empty');
        }
    
        const pathArray = path.split('.');
        let key = '';
        let currentValue;
        let i = 0;
        for (; i < pathArray.length; i++) {
            const subpath = pathArray[i];
            if (key !== '') {
                key += '.';
            }
            key += subpath;
    
            // Versuche, den Wert für den aktuellen Schlüssel zu lesen
            const storedString = this.readValue(key);
            if (storedString !== undefined && storedString !== null) {
                currentValue = this.toValue(storedString); // JSON mit "value" auspacken
                break;   
            }   
        }
    
        // Navigiere durch das Objekt, falls es verschachtelt ist und der Pfad noch nicht vollständig aufgelöst wurde
        if (currentValue !== undefined && currentValue !== null) {
            for (let j = i + 1; j < pathArray.length; j++) {
                currentValue = currentValue[pathArray[j]];
                if (currentValue === undefined) {
                    return undefined;
                }
            }
            return currentValue;
        }
    
        return undefined;
    }

    saveValue(path, value) {
        throw new Error('saveValue must be implemented in the subclass');
    }

    /**
     * @protected
     * @param {string} path 
     */
    readValue(path) {
        throw new Error('getValue must be implemented in the subclass');
    }

    removeValue(path) {
       throw new Error('removeValue must be implemented in the subclass');
    }

    /**
     * @protected
    * @param {string} path 
    */
    onEvent(event) {
        if (event.storageArea !== this.storageArea) return;
        this.startUpdate(event.key);
    }

    startUpdate(eventPath) {
        const textContentSet = this.pathMap[eventPath];
        if (textContentSet === undefined) {
            return;
        }
        for (const textContent of textContentSet) {
            textContent.doRefresh(); // initialtes reloading stored data
        }
    }


    /**
     * Saves a value to the storage area.  We can not refesh the text, content here, 
     * because the data from server is not yet stored !
     * 
     * @public
     * @param {{string: any}} values 
     */
    saveData(values) {
        for (var path of Object.keys(values)) {
            const val = values[path];
            if (typeof val === 'function') {
                // Ignore functions
                continue;
            }
            if (val === undefined || val === null) {
                this.removeValue(path);
            } else {
                this.saveValue(path, this.toStoreString(val));
            }
            this.startUpdate(path);
            this.eventPublisher.publish(this.eventKey, new StorageUpdateEvent(path, val, !val));
        }
       
    }

    /**
     * @private
     * @param {any} value 
     * @returns {string} json
     */
    toStoreString(value) {
        return JSON.stringify({ value: value });
    }


    /**
     * Converts the stored json string to a value.
     * 
     * @private
     * @param {string} storedString (json)
     * @returns {any}
     */
    toValue(storedString) {
        if (storedString === undefined || storedString === null) {
            return undefined;
        }
        var o = JSON.parse(storedString);
        if (o === undefined) {
            return undefined;
        }
        return o.value;
    }



}