package test.page.refresh;

import one.xis.context.IntegrationTestContext;
import one.xis.context.TestRefreshEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests refresh-event support in integration tests.
 * <p>
 * Two variants are demonstrated:
 * 1. simulatePushEvent() - directly from IntegrationTestContext
 * 2. TestRefreshEventPublisher injected into a service - the refresh event
 * arrives as a side-effect of calling the service method, exactly as
 * it would in production.
 */
class RefreshEventTest {

    private IntegrationTestContext context;

    @BeforeEach
    void init() {
        context = IntegrationTestContext.builder()
                .withSingleton(MatchPage.class)
                .withSingleton(ScoreBoard.class)
                // TestRefreshEventPublisher replaces the real RefreshEventPublisherImpl.
                // It is wired automatically by IntegrationTestContext with the JS mock.
                .withSingleton(new TestRefreshEventPublisher())
                .withSingleton(MatchService.class)
                .build();
    }

    @Test
    @DisplayName("simulatePushEvent() refreshes page that listens to the event key")
    void simulatePushEventRefreshesPage() {
        var result = context.openPage(MatchPage.class);

        assertThat(result.getDocument().getElementById("match-title").getInnerText())
                .isEqualTo("Home FC vs. Away United");

        // Simulate a server refresh event - MatchPage listens to "score-updated"
        var updated = context.simulatePushEvent("score-updated");

        assertThat(updated.getDocument().getElementById("match-title").getInnerText())
                .isEqualTo("Home FC vs. Away United");
    }

    @Test
    @DisplayName("simulatePushEvent() refreshes widget that listens to the event key")
    void simulatePushEventRefreshesWidget() {
        var result = context.openPage(MatchPage.class);

        assertThat(result.getDocument().getElementById("home-goals").getInnerText())
                .isEqualTo("0");

        assertThat(result.getDocument().getElementById("away-goals").getInnerText())
                .isEqualTo("0");

        // Directly manipulate the ScoreBoard state and trigger the refresh event.
        context.getSingleton(ScoreBoard.class).setScore(1, 0);
        context.simulatePushEvent("score-updated");

        assertThat(result.getDocument().getElementById("home-goals").getInnerText())
                .isEqualTo("1");

        assertThat(result.getDocument().getElementById("away-goals").getInnerText())
                .isEqualTo("0");
    }


    @Test
    @DisplayName("Multiple goals accumulate correctly via refresh events")
    void multipleGoalsAccumulateCorrectly() {
        var result = context.openPage(MatchPage.class);

        var matchService = context.getSingleton(MatchService.class);
        matchService.homeGoal();
        matchService.homeGoal();
        matchService.awayGoal();
        context.simulatePushEvent("score-updated");

        assertThat(result.getDocument().getElementById("home-goals").getInnerText())
                .isEqualTo("2");

        assertThat(result.getDocument().getElementById("away-goals").getInnerText())
                .isEqualTo("1");
    }

    @Test
    @DisplayName("Refresh event with wrong key does not refresh the widget")
    void wrongEventKeyDoesNotRefreshWidget() {
        var result = context.openPage(MatchPage.class);

        context.getSingleton(ScoreBoard.class).setScore(3, 2);

        // "other-event" is not in @RefreshOnUpdateEvents("score-updated")
        context.simulatePushEvent("other-event");

        // Score should still show 0:0 - widget was not refreshed
        assertThat(result.getDocument().getElementById("home-goals").getInnerText())
                .isEqualTo("0");

        assertThat(result.getDocument().getElementById("away-goals").getInnerText())
                .isEqualTo("0");
    }

    @Test
    @DisplayName("Refresh event fired by simulation method is received by the page")
    void refreshEventFiredBySimulationMethodIsReceivedByPage() {
        var result = context.openPage(MatchPage.class);

        var scoreBoard = context.getSingleton(ScoreBoard.class);
        scoreBoard.setMinutes(10);

        context.simulatePushEvent("minutes-updated");

        assertThat(result.getDocument().getElementById("minutes").getInnerText())
                .isEqualTo("10");

    }


}
