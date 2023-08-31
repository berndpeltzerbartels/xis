package micronaut.example

import one.xis.ModelData
import one.xis.Page
import one.xis.WelcomePage

@WelcomePage
@Page('/index.html')
class Index {

    @ModelData('pages')
    def pages() {
        [new PageLink('repeat', 'Repeat', '/repeat.html'), new PageLink('repeatInsideRepeat', 'RepeatInsideRepeat', '/repeatInsideRepeat.html')]
    }
}
