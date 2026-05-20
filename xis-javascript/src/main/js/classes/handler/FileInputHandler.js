class FileInputHandler extends InputTagHandler {

    constructor(input) {
        super(input);
        this.type = 'file-handler';
    }

    refresh(data) {
        const result = super.refresh(data);
        const formHandler = this.getParentFormHandler();
        formHandler.tag.setAttribute('enctype', 'multipart/form-data');
        formHandler.fileInputHandlers.push(this);
        return result;
    }

    refreshFormData(data) {
        // Browsers do not allow setting file input values from application data.
    }

    getValue() {
        return undefined;
    }

    getUploads() {
        const files = this.tag.files;
        if (!files || files.length === 0) {
            return [];
        }
        const fieldName = this.binding || this.tag.getAttribute('name') || this.tag.getAttribute('id');
        const uploads = [];
        for (let i = 0; i < files.length; i++) {
            const file = typeof files.item === 'function' ? files.item(i) : files[i];
            uploads.push({
                fieldName: fieldName,
                file: file
            });
        }
        return uploads;
    }
}
