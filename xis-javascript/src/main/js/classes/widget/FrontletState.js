

class FrontletState {

    /**
     * 
     * @param {ResolvedURL} resolvedURL 
     * @param {{string: string}} frontletParameters
     */
    constructor(resolvedURL, frontletParameters) {
        this.resolvedURL = resolvedURL;
        this.frontletParameters = frontletParameters;
    }
}
