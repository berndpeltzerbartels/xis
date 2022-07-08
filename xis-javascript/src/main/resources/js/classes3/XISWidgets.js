class XISWidgets {


    getWidget(id) {
        var widget = this.getAllWidgets()[id];
        widget.init();
        return widget;
    }


    /**
     * @returns {any}
     */
    getAllWidgets() {
        throw new Error('abstract method');
    }
    
}