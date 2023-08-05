/**
 * @typedef PageAttributes
 * @property {string} host
 * @property {array<string>} modelsToSubmitOnRefresh
 * @property {{string: array<string>}} modelsToSubmitOnAction
 * @property {Path} path
 * @property {string} normalizedPath
 * @property {boolean} welcomePage
 */

class PageAttributes {


    constructor(obj) {
        this.host = obj.host;
        this.modelsToSubmitOnRefresh = obj.modelsToSubmitOnRefresh;
        this.modelsToSubmitOnAction = obj.modelsToSubmitOnAction;
        this.path = new Path(new PathElement(obj.path.pathElement));
        this.normalizedPath = obj.normalizedPath;
        this.welcomePage = obj.welcomePage;
    }
}
