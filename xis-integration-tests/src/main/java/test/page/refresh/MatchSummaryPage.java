package test.page.refresh;

import one.xis.ModelData;
import one.xis.Page;

@Page("/matchSummary.html")
class MatchSummaryPage {

    @ModelData("summary")
    String summary() {
        return "Match summary";
    }
}
