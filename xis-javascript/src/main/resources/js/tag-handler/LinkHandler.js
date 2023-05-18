class LinkHandler extends TagHandler {

    constructor(element) {
        super(element);
        this.type = 'link-handler';
        this.pageExpression = this.expressionFromAttribute('page');
        this.widgetExpression = this.expressionFromAttribute('widget');
        this.targetExpression = this.expressionFromAttribute('target-container');
        this.refreshExpression = this.expressionFromAttribute('refresh');
        if (element.localName == 'a') {
            element.setAttribute('href', '#');
        }
    }

    refresh(data) {
        var _this = this; // TODO validate attributes in backend
        this.data = data;
        if (this.pageExpression) {
            this.pageId = this.pageExpression.evaluate(data);
        }
        if (this.widgetExpression) {
            this.widgetId = this.widgetExpression.evaluate(data);
        }
        if (this.targetExpression) {
            this.targetContainerId = this.targetExpression.evaluate(data);
        }
        if (this.refreshExpression) {
            this.refreshFlag = this.refreshExpression.evaluate(data);
        } else {
            this.refreshFlag = true;
        }
        this.element.onclick = e => _this.onClick(e);
    }


    onClick(e) {
        if (this.widgetExpression) {
            this.onClickWidgetLink(this.widgetId);
        } else if (this.pageExpression) {
            this.onClickPageLink(this.pageId);
        }
    }

    onClickWidgetLink() {
        var container = this.getTargetContainer();
        var handler = container._handler;
        handler.showWidget(this.widgetId);
        if (this.refreshFlag) {
            handler.reloadDataAndRefresh(this.data);
        }
    }

    onClickPageLink(pageId) {
        bindPageId(pageId);
        if (this.refreshFlag) {
            reloadDataAndRefreshCurrentPage();
        }
    }




    asString() {
        return 'Link';
        // TODO
    }


}


