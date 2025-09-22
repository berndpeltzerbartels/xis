class Store {
    /**
     * @param {Storage} storageArea - The storage area to use (e.g., localStorage or sessionStorage)
     */
    constructor(storageArea) {
        this.storageArea = storageArea;
        this.paths = []
        this.pathMap = {};
        window.addEventListener('storage', (event) => this.onEvent(event));
    }

    /**
     * Adds a path to the list of paths to be activated.
     * This method is used to track paths that need to be monitored for changes.
     * The paths are stored in the `paths` array and will be mapped to handlers later.
     * 
     * @public
     * @param {string} path 
     * @param {Refreshable} refreshable
     */
    registerListener(path, refreshable) {
            this.paths.push(path);
        }


    /**
     * Maps the current text content to all listeners and clears the listener list.
     * @param {TextContent>} textContent 
     */
    mapTextContent(textContent) {
        for (const path of this.paths) {
            var key = '';
            for (var subpath of path.split('.')) {
                if (key !== '') {
                    key += '.';
                }
                key += subpath;

                if (this.pathMap[key] === undefined) {
                    this.pathMap[key] = new Set();
                }
                this.pathMap[key].add(textContent);
            }
        }
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
                this.saveValue(path, undefined);
            } else {
                this.saveValue(path, this.toStoreString(val));
            }
            this.startUpdate(path);
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