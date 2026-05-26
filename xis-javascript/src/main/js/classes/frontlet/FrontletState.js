

class FrontletState {

    /**
     * 
     * @param {ResolvedURL} resolvedURL 
     * @param {{string: string}} frontletParameters
     * @param {{string: string}} modalParameters
     */
    constructor(resolvedURL, frontletParameters, modalParameters = {}) {
        this.resolvedURL = resolvedURL;
        this.frontletParameters = frontletParameters;
        this.modalParameters = modalParameters;
    }
}
