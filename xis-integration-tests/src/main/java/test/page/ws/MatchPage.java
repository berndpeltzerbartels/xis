package test.page.ws;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.RefreshOnUpdateEvents;

@Page("/matchPage.html")
@RefreshOnUpdateEvents("score-updated")
class MatchPage {

    @ModelData("matchTitle")
    String getMatchTitle() {
        return "Home FC vs. Away United";
    }
}
