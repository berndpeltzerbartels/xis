/**
 * @typedef PageAttributes
 * @property {string} host
 * @property {array<string>} modelParameterNames
 * @property {{string: array<string>}} actionParameterNames
 * @property {Path} path
 * @property {string} normalizedPath
 * @property {boolean} welcomePage
 * @property {string} pageJavascriptSource
 */

class PageAttributes {


    constructor(obj) {
        this.host = obj.host;
        this.modelParameterNames = obj.modelParameterNames || [];
        this.actionParameterNames = obj.actionParameterNames || {};
        this.path = new Path(new PathElement(obj.path.pathElement));
        this.normalizedPath = obj.normalizedPath;
        this.welcomePage = obj.welcomePage;
        this.pageJavascriptSource = obj.pageJavascriptSource;
        this.globalScopeKeys = obj.globalScopeKeys || [];
        this.localStorageKeys = obj.localStorageKeys || [];
        this.clientStateKeys = obj.clientStateKeys || [];
    }
}
