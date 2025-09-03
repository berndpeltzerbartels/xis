class InputTagHandler extends FormElementHandler {

    constructor(input) {
        super(input);
        this.type = input.getAttribute('type');
        if (this.type=='file') {
           this.getParentFormHandler().fileInputHandlers.push(this);
        }
    }
}