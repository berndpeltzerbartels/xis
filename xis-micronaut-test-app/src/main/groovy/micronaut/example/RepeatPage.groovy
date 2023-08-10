package micronaut.example

import one.xis.Model
import one.xis.Page

@Page('/repeat.html')
class RepeatPage {

    @Model('items')
    def items() {
        [new RepeatPageItem(1, 'title1'), new RepeatPageItem(2, 'title2'), new RepeatPageItem(3, 'title3')]
    }
}
