package micronaut.example

import one.xis.OnInit
import one.xis.Page

@Page(path = "/product/details", welcomePage = true)
class ProductPage {

    @OnInit
    Map<String, String> pageAttributes() {
    }
}
