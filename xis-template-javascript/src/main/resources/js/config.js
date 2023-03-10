
/**
 * @property {array<ComponentConfig>} pageConfigs
 * @property {array<ComponentConfig>} widgetConfigs
 * @property {string} welcomePageId
 */
class Config {

    constructor() {
        this.pageConfigs = [];
        this.widgetConfigs = [];
        this.welcomePageId = undefined;
    }

    getPageConfig(id) {
        return this.pageConfigs.find(config => config.id == id);
    }

    getWidgetConfig(id) {
        return this.find(config => config.id == id);
    }
}

/**
 * @property {string} id
 * @property {array<MethodConfig>} initMethods
 * @property {array<MethodConfig>} showMethods
 * @property {array<MethodConfig>} hideMethods
 * @property {array<MethodConfig>} destroyMethods
 */
class ComponentConfig {

    constructor() {
        this.id = undefined;
        this.initMethods = [];
        this.showMethods = [];
        this.hideMethods = [];
        this.destroyMethods = [];
    }

    hasInitMethods() {
        return this.initMethods.length > 0;
    }

    hasShowMethods() {
        return this.showMethods.length > 0;
    }

    hasHideMethods() {
        return this.hideMethods.length > 0;
    }

    hasDestroyMethods() {
        return this.destroyMethods.length > 0;
    }

}

/**
 * @property {string} name
 * @property {array<ParameterConfig>} parameters     
 */
class MethodConfig {
    constructor() {
        this.name = undefined;
        this.parameters = [];
    }
}

/**
 * @property {string} name
 * @property {array<string>} path
 */
class ParameterConfig {
    constructor() {
        this.name = '';
        this.path = [];
    }
}