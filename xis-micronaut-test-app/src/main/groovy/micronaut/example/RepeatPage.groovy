package micronaut.example


import one.xis.ModelData
import one.xis.Page

@Page('/repeat.html')
class RepeatPage {

    @ModelData('items')
    def items() {
        [new RepeatPageItem(1, 'title1'), new RepeatPageItem(2, 'title2'), new RepeatPageItem(3, 'title3')]
    }
}
