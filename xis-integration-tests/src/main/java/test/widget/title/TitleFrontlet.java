package test.widget.title;

import one.xis.Title;
import one.xis.Frontlet;

@Frontlet
public class TitleFrontlet {

    @Title
    String getTitle() {
        return "Mein neuer Titel";
    }
}
