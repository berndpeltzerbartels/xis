package test.page.ws;

import one.xis.ModelData;
import one.xis.RefreshOnUpdateEvents;
import one.xis.Widget;

@Widget
@RefreshOnUpdateEvents({"score-updated", "minutes-updated"})
class ScoreBoard {

    private int homeGoals = 0;
    private int awayGoals = 0;
    private int minutes = 0;

    void setScore(int homeGoals, int awayGoals) {
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
    }

    void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    @ModelData("homeGoals")
    int getHomeGoals() {
        return homeGoals;
    }

    @ModelData("awayGoals")
    int getAwayGoals() {
        return awayGoals;
    }

    @ModelData
    int minutes() {
        return minutes;
    }

}
