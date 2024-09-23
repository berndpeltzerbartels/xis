

class WidgetState {

    /**
     * 
     * @param {ResolvedURL} resolvedURL 
     * @param {{string: string}} widgetParameters 
     */
    constructor(resolvedURL, widgetParameters) {
        this.resolvedURL = resolvedURL;
        this.widgetParameters = widgetParameters;
    }
}