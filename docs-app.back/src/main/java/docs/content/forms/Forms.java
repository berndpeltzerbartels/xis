package docs.content.forms;

import docs.Navigation;
import docs.content.XisTheme;
import one.xis.ClientState;
import one.xis.context.XISComponent;

@XISComponent
@Navigation(title = "Forms", firstSubItem = FormDataAndBindings.class, nextItem = XisTheme.class)
public class Forms {

    @ClientState("title")
    String title() {
        return "Forms";
    }

    @ClientState("headline1")
    String headline1() {
        return "Forms";
    }
}
