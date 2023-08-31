package micronaut.example


import one.xis.ModelData
import one.xis.Page
import one.xis.PathVariable

@Page('/pathvar/{x}.html')
class PathVariablePage {

    @ModelData('x')
    def variable(@PathVariable('x') def x) {
        x
    }
}
