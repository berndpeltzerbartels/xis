package micronaut.example

import one.xis.Model
import one.xis.Page
import one.xis.WelcomePage

@WelcomePage
@Page('/index.html')
class Index {

    @Model('pages')
    def pages() {
        [new PageLink('repeat', 'Repeat', '/repeat.html'), new PageLink('repeatInsideRepeat', 'RepeatInsideRepeat', '/repeatInsideRepeat.html')]
    }
}
