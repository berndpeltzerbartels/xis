class InputTagHandler extends FormElementHandler {

    constructor(input) {
        super(input);
        this.type = input.getAttribute('type');
    }
}
