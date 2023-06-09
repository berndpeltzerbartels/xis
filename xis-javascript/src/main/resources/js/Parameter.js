class Parameter {

    /**
     * @param {string} name 
     * @param {string} expressionSrc 
     */
    constructor(name, expressionSrc) {
        this.name = name;
        this.expression = new TextContentParser(expressionSrc).parse();
        this.value = undefined;
    }

    /**
     * @public
     * @param {Data} data 
     */
    refresh(data) {
        this.value = this.expression.evaluate(data);
    }
}