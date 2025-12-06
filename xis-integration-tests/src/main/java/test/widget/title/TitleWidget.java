package test.widget.title;

import one.xis.Title;
import one.xis.Widget;

@Widget
public class TitleWidget {

    @Title
    String getTitle() {
        return "Mein neuer Titel";
    }
}
