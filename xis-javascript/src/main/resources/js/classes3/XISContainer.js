/**
 * Element for displaying widgets
 */
class XISContainer extends XISTemplateObject {

    /**
    * @param {XISTemplateObject} parent
    */
    constructor(parent) {
        super(parent);
    }

    /**
    * @param {XISTemplateObject} parent 
    * @param {XISValueHolder} valueHolder
    */
    init(parent, valueHolder) {
        // TODO vermutlich nicht in Gebrauch
        super.init(parent, valueHolder);
        parent.appendChild(this.element);
        if (this.defaultWidgetId) {
            this.setWidget(this.defaultWidgetId);
        }
    }

    refresh() {
        // TODO
    }

    /**
     * @override
     * @returns {XISContainer}
     */
    getContainer() {
        return this;
    }

    setWidget(widgetName) {
        // TODO
        if (this.widget) {
            if (this.widget.name == widgetName) {
                __lifecycleService.onDisplayWidget(this.widget);
                return;
            }
            __lifecycleService.onHideWidget(this.widget);
            this.widget.unbind();
        }
        this.widget = __widgets.getWidget(widgetName);
        if (!this.widget.initialized) {
            this.widget.initialized = true;
            this.widget.init();
            __lifecycleService.onInitWidget(this.widget);
        }
        this.widget.bind(this.element, this.valueHolder);
        __lifecycleService.onDisplayWidget(this.widget);
    }

    update() {
        this.updateAttributes();
        if (this.widget) {
            this.widget.update();
        }
    }

    updateAttributes() {
        throw new Error('updateAttributes is abstract in ' + this);
    }

    updateAttribute(name, value) {
        this.element.setAttribute(name, value);
    }

    unlink() {
        this.parent.removeChild(this.element);
    }

    /**
     * @override
     * @param {Node} element 
     */
    removeChild(element) {
        this.element.removeChild(element);
    }

}
