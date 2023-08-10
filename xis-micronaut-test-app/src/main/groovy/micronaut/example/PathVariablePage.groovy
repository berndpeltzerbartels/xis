package micronaut.example

import one.xis.Model
import one.xis.Page
import one.xis.PathVariable

@Page('/pathvar/{x}.html')
class PathVariablePage {

    @Model('x')
    def variable(@PathVariable('x') def x) {
        x
    }
}
