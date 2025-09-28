package docs.content;

import docs.Navigation;
import docs.content.templatesyntax.TemplateSynthax;
import one.xis.ClientState;
import one.xis.Widget;

@Widget
@Navigation(title = "Micro-Frontend Architecture", nextItem = TemplateSynthax.class)
class MicroFrontendArchitecture {

    @ClientState("title")
    String title() {
        return "Micro-Frontend Architecture";
    }

    @ClientState("headline1")
    String headline1() {
        return "Micro-Frontend Architecture";
    }

    @ClientState("headline2")
    String headline2() {
        return null;
    }
}
