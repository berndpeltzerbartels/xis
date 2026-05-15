package test.frontlet.title;

import one.xis.Address;
import one.xis.Title;
import one.xis.Frontlet;

@Frontlet
public class TitleFrontlet {

    @Title
    String getTitle() {
        return "Mein neuer Titel";
    }

    @Address
    String getAddress() {
        return "/frontlet-address-should-not-reach-browser.html";
    }
}
