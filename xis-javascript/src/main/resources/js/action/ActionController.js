class ActionController {

    // TODO remove this class ?

    /**
     * 
     * @param {PageController} pageController
     * @param {Widgets} widgets 
     * @param {Client} client
     */
    constructor(pageController, widgets, client) {
        this.pageController = pageController;
        this.widgets = widgets;
        this.client = client;
    }


    /**
     * @public
     * @param {string} pageId
     * @param {Element} invokerElement
     * @param {string} action
     * @param {} data 
     */
    action(invokerElement, action, data) {
        var container = this.parentWidgetContainer(invokerElement);
        var widgetId = container ? container.widgetId : undefined;
        var data = container._handler.data;
        this.client.action(pageId, widgetId, action, data).then(response => {
            if (response.nextPageId && pageId !== response.nextPageId) {

            }
            if (response.nextWidgetId && widgetId !== response.nextWidgetId) {
                var widgetRoot = this.widgets.getWidgetRoot(widgetId);
                container._handler.showWidget(widgetRoot);
            }
            if (container) {
                container._handler.refresh(response.data);
            }
        });

    }

    /**
    * @public
    * @param {string} widgetId
    * @param {Element} invokerElement
    * @param {string} action
    * @param {} data 
    */
    submitWidgetAction(widgetId, invokerElement, action, data) {
        var container = this.parentWidgetContainer(invokerElement);
        if (!container) {
            throw new Error('no parent widget-container for action: ' + action);
        }
        var data = container._handler.data;
        this.client.widgetAction(widgetId, action, data).then(response => {
            if (widgetId !== response.nextWidgetId) {
                var widgetRoot = this.widgets.getWidgetRoot(widgetId);
                container._handler.showWidget(widgetRoot);
            }
            container._handler.refresh(response.data);
        });
    }

    /**
     * @private
     * @param {Response} response 
     */
    handlePageActionResponse(response) {
        if (response.pageId) {
            this.showPage(response.pageId);
        }
        if (response.widgetId) {
            this.showWidget(response.widgetId);
        }
    }

    showPage(pageId) {
        this.pageController.bindPage(pageId);
    }

    showWidget(widgetId) {

    }

    /**
     * @private
     * @param {Element} element 
     */
    parentWidgetContainer(element) {
        var e = element;
        while (e != null) {
            if (e.localName == 'xis:widget-container') {
                return e;
            }
        };
    }

}