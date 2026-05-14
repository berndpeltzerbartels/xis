package one.xis.context;

class CronUtil {

    /**
     * Calculates the next start time for a fixed-delay scheduled task.
     * Fixed delay means the delay is measured from the END of the last execution.
     *
     * @param scheduleBegin    The time when the schedule was initialized
     * @param initialDelay     The initial delay before the first execution
     * @param fixedDelayMillis The delay between the end of one execution and the start of the next
     * @param lastStartTime    The start time of the last execution (< 0 if never executed)
     * @return The absolute time in millis when the next execution should start
     */
    static long nextStartTimeFixedDelay(long scheduleBegin, long initialDelay, long fixedDelayMillis, long lastStartTime) {
        // First execution: schedule begin + initial delay
        if (lastStartTime < 0) {
            return scheduleBegin + initialDelay;
        }

        // Subsequent executions: current time (task just ended) + fixed delay
        long currentTime = System.currentTimeMillis();
        return currentTime + fixedDelayMillis;
    }

    /**
     * Calculates the next start time for a fixed-rate scheduled task.
     * Fixed rate means executions happen at regular intervals from the schedule begin,
     * regardless of execution time (tasks may overlap if execution takes too long).
     *
     * @param scheduleBegin   The time when the schedule was initialized
     * @param initialDelay    The initial delay before the first execution
     * @param fixedRateMillis The fixed interval between execution starts
     * @param lastStartTime   The start time of the last execution (not used, but kept for symmetry)
     * @return The absolute time in millis when the next execution should start
     */
    static long nextStartTimeFixedRate(long scheduleBegin, long initialDelay, long fixedRateMillis, long lastStartTime) {
        long currentTime = System.currentTimeMillis();
        long baseTime = scheduleBegin + initialDelay;

        // Calculate how many periods have elapsed since the base time
        long elapsedSinceBase = currentTime - baseTime;

        // If we haven't reached the base time yet, return base time
        if (elapsedSinceBase < 0) {
            return baseTime;
        }

        // Calculate next start time based on fixed rate
        long periodsElapsed = elapsedSinceBase / fixedRateMillis;
        long nextStartTime = baseTime + (periodsElapsed + 1) * fixedRateMillis;

        // Ensure we don't return a time in the past (safety check)
        return Math.max(currentTime, nextStartTime);
    }

    /**
     * Calculates the next start time based on a cron expression.
     *
     * @param cronExpression The cron expression (not yet implemented)
     * @return The absolute time in millis when the next execution should start
     */
    static long nextStartTimeCronExpression(String cronExpression) {
        throw new UnsupportedOperationException("Cron expressions are not yet supported");
    }

}
