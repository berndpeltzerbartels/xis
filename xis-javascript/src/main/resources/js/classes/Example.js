class ProductListWidget extends XISWidget {

    /**
     * @override
     */
    createChildren() {
        return [new ProductList()];
    }

}

class ProductList extends XISElement {

    /**
     * @override
     */
    createElement() {
        return createElement('li', {});
    }

}


class ProductItem extends XISElement {}