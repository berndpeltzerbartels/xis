class LinkHandler extends TagHandler {

    constructor(element) {
        super(element);
        this.type = 'link-handler';
        this.pageExpression = this.expressionFromAttribute('xis:page');
        this.widgetExpression = this.expressionFromAttribute('xis:widget');
        this.targetExpression = this.expressionFromAttribute('xis:target-container');
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
        this.tag.onclick = e => { _this.onClick(e).catch(e => console.error(e)); };
    }


    onClick(e) {
        if (this.widgetExpression) {
            return this.onClickWidgetLink(this.widgetId);
        } else if (this.pageExpression) {
            return this.onClickPageLink(this.pageId);
        }
    }

    onClickWidgetLink() {
        return new Promise((resolve, _) => {
            var container = this.getTargetContainer();
            var handler = container._handler;
            var _this = this;
            handler.showWidget(this.widgetId);
            handler.reloadDataAndRefresh(this.data);
            resolve();
        });
    }

    onClickPageLink(pageId) {
        return bindPage(pageId).then(() => reloadDataAndRefreshCurrentPage()).catch(e => console.error(e));
    }




    asString() {
        return 'Link';
        // TODO
    }


}


