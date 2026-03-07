package test.page.ws;

import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.ws.RefreshEventPublisher;

/**
 * Simulates a backend service that updates the score and notifies
 * all clients via a WebSocket push event.
 */
@Component
@RequiredArgsConstructor
public class MatchService {

    private final RefreshEventPublisher refreshEventPublisher;
    private final ScoreBoard scoreBoard;

    private int homeGoals = 0;
    private int awayGoals = 0;

    public void homeGoal() {
        scoreBoard.setScore(++homeGoals, awayGoals);
        refreshEventPublisher.publishToAll("score-updated");
    }

    public void awayGoal() {
        scoreBoard.setScore(homeGoals, ++awayGoals);
        refreshEventPublisher.publishToAll("score-updated");
    }

    public void reset() {
        homeGoals = 0;
        awayGoals = 0;
        scoreBoard.setScore(0, 0);
        refreshEventPublisher.publishToAll("score-updated");
    }
}
