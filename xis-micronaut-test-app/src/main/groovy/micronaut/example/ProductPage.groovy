package micronaut.example

import one.xis.OnInit
import one.xis.Page

@Page(value = "/product/details")
class ProductPage {

    @OnInit
    Map<String, String> pageAttributes() {
    }
}
