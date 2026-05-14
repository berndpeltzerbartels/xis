package one.xis.context;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CronUtilTest {

    @Test
    void testNextStartTimeFixedDelay_FirstExecution() {
        long scheduleBegin = 1000L;
        long initialDelay = 500L;
        long fixedDelayMillis = 1000L;
        long lastStartTime = -1; // Never executed

        long nextStart = CronUtil.nextStartTimeFixedDelay(scheduleBegin, initialDelay, fixedDelayMillis, lastStartTime);

        assertEquals(1500L, nextStart, "First execution should be scheduleBegin + initialDelay");
    }

    @Test
    void testNextStartTimeFixedDelay_SubsequentExecution() {
        long scheduleBegin = 1000L;
        long initialDelay = 500L;
        long fixedDelayMillis = 2000L;
        long lastStartTime = 5000L; // Last task started at 5000

        long beforeCall = System.currentTimeMillis();
        long nextStart = CronUtil.nextStartTimeFixedDelay(scheduleBegin, initialDelay, fixedDelayMillis, lastStartTime);
        long afterCall = System.currentTimeMillis();

        // Next start should be current time + fixed delay
        assertTrue(nextStart >= beforeCall + fixedDelayMillis, "Should be at least currentTime + fixedDelay");
        assertTrue(nextStart <= afterCall + fixedDelayMillis + 10, "Should not be too far in the future");
    }

    @Test
    void testNextStartTimeFixedDelay_ZeroInitialDelay() {
        long scheduleBegin = 1000L;
        long initialDelay = 0L;
        long fixedDelayMillis = 1000L;
        long lastStartTime = -1;

        long nextStart = CronUtil.nextStartTimeFixedDelay(scheduleBegin, initialDelay, fixedDelayMillis, lastStartTime);

        assertEquals(1000L, nextStart, "With zero initial delay, first execution should be at scheduleBegin");
    }

    @Test
    void testNextStartTimeFixedRate_BeforeBaseTime() {
        long currentTime = System.currentTimeMillis();
        long scheduleBegin = currentTime + 10000; // 10 seconds in the future
        long initialDelay = 5000L;
        long fixedRateMillis = 1000L;
        long lastStartTime = -1;

        long nextStart = CronUtil.nextStartTimeFixedRate(scheduleBegin, initialDelay, fixedRateMillis, lastStartTime);

        assertEquals(scheduleBegin + initialDelay, nextStart,
                "If schedule hasn't started yet, return baseTime (scheduleBegin + initialDelay)");
    }

    @Test
    void testNextStartTimeFixedRate_FirstPeriod() {
        long currentTime = System.currentTimeMillis();
        long scheduleBegin = currentTime - 5000; // 5 seconds ago
        long initialDelay = 1000L;
        long fixedRateMillis = 10000L; // 10 seconds
        long lastStartTime = -1;

        long nextStart = CronUtil.nextStartTimeFixedRate(scheduleBegin, initialDelay, fixedRateMillis, lastStartTime);

        // Base time = currentTime - 5000 + 1000 = currentTime - 4000
        // We are still in the first period (less than 10 seconds elapsed)
        // Next start = baseTime + 10000 = currentTime - 4000 + 10000 = currentTime + 6000
        long baseTime = scheduleBegin + initialDelay;
        long expectedNextStart = baseTime + fixedRateMillis;

        assertTrue(nextStart >= currentTime, "Next start should not be in the past");
        assertTrue(nextStart <= expectedNextStart + 100, "Next start should be close to expected value");
    }

    @Test
    void testNextStartTimeFixedRate_MultiplePeriods() {
        long currentTime = System.currentTimeMillis();
        long scheduleBegin = currentTime - 25000; // 25 seconds ago
        long initialDelay = 0L;
        long fixedRateMillis = 10000L; // 10 seconds
        long lastStartTime = -1;

        long nextStart = CronUtil.nextStartTimeFixedRate(scheduleBegin, initialDelay, fixedRateMillis, lastStartTime);

        // Base time = currentTime - 25000
        // Elapsed = 25000ms
        // Periods elapsed = 25000 / 10000 = 2 (floor)
        // Next start = baseTime + (2 + 1) * 10000 = currentTime - 25000 + 30000 = currentTime + 5000
        long baseTime = scheduleBegin + initialDelay;
        long elapsed = currentTime - baseTime;
        long periodsElapsed = elapsed / fixedRateMillis;
        long expectedNextStart = baseTime + (periodsElapsed + 1) * fixedRateMillis;

        assertTrue(nextStart >= currentTime, "Next start should not be in the past");
        assertEquals(expectedNextStart, nextStart, 100, "Next start should match calculated period");
    }

    @Test
    void testNextStartTimeFixedRate_VeryLongExecution() {
        long currentTime = System.currentTimeMillis();
        long scheduleBegin = currentTime - 100000; // 100 seconds ago
        long initialDelay = 0L;
        long fixedRateMillis = 1000L; // 1 second
        long lastStartTime = -1;

        long nextStart = CronUtil.nextStartTimeFixedRate(scheduleBegin, initialDelay, fixedRateMillis, lastStartTime);

        // Even with very long execution, next start should not be in the past
        assertTrue(nextStart >= currentTime,
                "Even after missing many periods, next start should be current time or future");
    }

    @Test
    void testNextStartTimeFixedRate_ExactPeriodBoundary() {
        long scheduleBegin = 1000L;
        long initialDelay = 0L;
        long fixedRateMillis = 1000L;

        // Simulate current time being exactly at a period boundary
        long baseTime = scheduleBegin + initialDelay;
        long currentTime = baseTime + 5 * fixedRateMillis; // Exactly 5 periods later

        // Mock current time by using past scheduleBegin
        long adjustedScheduleBegin = currentTime - 5 * fixedRateMillis;

        long nextStart = CronUtil.nextStartTimeFixedRate(adjustedScheduleBegin, initialDelay, fixedRateMillis, -1);

        // Should schedule for next period
        assertTrue(nextStart > currentTime, "Should schedule next period even at exact boundary");
    }

    @Test
    void testNextStartTimeFixedRate_WithInitialDelay() {
        long currentTime = System.currentTimeMillis();
        long scheduleBegin = currentTime - 15000; // 15 seconds ago
        long initialDelay = 5000L; // 5 seconds
        long fixedRateMillis = 10000L; // 10 seconds
        long lastStartTime = -1;

        long nextStart = CronUtil.nextStartTimeFixedRate(scheduleBegin, initialDelay, fixedRateMillis, lastStartTime);

        // Base time = currentTime - 15000 + 5000 = currentTime - 10000
        // Elapsed = 10000ms
        // Periods elapsed = 10000 / 10000 = 1
        // Next start = baseTime + 2 * 10000 = currentTime - 10000 + 20000 = currentTime + 10000
        long baseTime = scheduleBegin + initialDelay;
        long expectedNextStart = baseTime + 2 * fixedRateMillis;

        assertTrue(nextStart >= currentTime, "Next start should not be in the past");
        assertTrue(Math.abs(nextStart - expectedNextStart) <= 100,
                "Next start should match expected calculation within tolerance");
    }

    @Test
    void testNextStartTimeCronExpression_NotImplemented() {
        assertThrows(UnsupportedOperationException.class, () -> {
            CronUtil.nextStartTimeCronExpression("0 0 * * * *");
        }, "Cron expressions should throw UnsupportedOperationException");
    }

    @Nested
    @Disabled
    class CronExpressionTests {

        @Test
        void testCronExpression_EveryDayAtNoon() {
            // "0 0 12 * * ?" - Every day at 12:00:00
            long currentTime = System.currentTimeMillis();

            long nextStart = CronUtil.nextStartTimeCronExpression("0 0 12 * * ?");

            // Should be today at 12:00 or tomorrow at 12:00
            assertTrue(nextStart > currentTime, "Next start should be in the future");

            // Verify it's actually at 12:00
            java.time.LocalDateTime nextDateTime = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(nextStart),
                    java.time.ZoneId.systemDefault()
            );
            assertEquals(12, nextDateTime.getHour(), "Should be at 12:00");
            assertEquals(0, nextDateTime.getMinute(), "Should be at 12:00");
            assertEquals(0, nextDateTime.getSecond(), "Should be at 12:00");
        }

        @Test
        void testCronExpression_EveryMondayAt9AM() {
            // "0 0 9 ? * MON" - Every Monday at 09:00:00
            long nextStart = CronUtil.nextStartTimeCronExpression("0 0 9 ? * MON");

            java.time.LocalDateTime nextDateTime = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(nextStart),
                    java.time.ZoneId.systemDefault()
            );

            assertEquals(java.time.DayOfWeek.MONDAY, nextDateTime.getDayOfWeek(), "Should be Monday");
            assertEquals(9, nextDateTime.getHour(), "Should be at 09:00");
            assertEquals(0, nextDateTime.getMinute(), "Should be at 09:00");
        }

        @Test
        void testCronExpression_FirstDayOfMonthAtMidnight() {
            // "0 0 0 1 * ?" - First day of every month at midnight
            long nextStart = CronUtil.nextStartTimeCronExpression("0 0 0 1 * ?");

            java.time.LocalDateTime nextDateTime = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(nextStart),
                    java.time.ZoneId.systemDefault()
            );

            assertEquals(1, nextDateTime.getDayOfMonth(), "Should be first day of month");
            assertEquals(0, nextDateTime.getHour(), "Should be at midnight");
        }

        @Test
        void testCronExpression_Every15Minutes() {
            // "0 */15 * * * ?" - Every 15 minutes
            long currentTime = System.currentTimeMillis();
            long nextStart = CronUtil.nextStartTimeCronExpression("0 */15 * * * ?");

            assertTrue(nextStart > currentTime, "Should be in the future");

            java.time.LocalDateTime nextDateTime = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(nextStart),
                    java.time.ZoneId.systemDefault()
            );

            // Minute should be 0, 15, 30, or 45
            int minute = nextDateTime.getMinute();
            assertTrue(minute == 0 || minute == 15 || minute == 30 || minute == 45,
                    "Minute should be 0, 15, 30, or 45, but was: " + minute);
            assertEquals(0, nextDateTime.getSecond(), "Seconds should be 0");
        }

        @Test
        void testCronExpression_LastDayOfMonth() {
            // "0 0 12 L * ?" - Last day of month at noon
            long nextStart = CronUtil.nextStartTimeCronExpression("0 0 12 L * ?");

            java.time.LocalDateTime nextDateTime = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(nextStart),
                    java.time.ZoneId.systemDefault()
            );

            // Should be last day of the month
            int lastDay = nextDateTime.toLocalDate().lengthOfMonth();
            assertEquals(lastDay, nextDateTime.getDayOfMonth(), "Should be last day of month");
            assertEquals(12, nextDateTime.getHour(), "Should be at noon");
        }

        @Test
        void testCronExpression_WeekdaysOnly() {
            // "0 0 9 ? * MON-FRI" - Weekdays at 9 AM
            long nextStart = CronUtil.nextStartTimeCronExpression("0 0 9 ? * MON-FRI");

            java.time.LocalDateTime nextDateTime = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(nextStart),
                    java.time.ZoneId.systemDefault()
            );

            java.time.DayOfWeek dayOfWeek = nextDateTime.getDayOfWeek();
            assertTrue(dayOfWeek != java.time.DayOfWeek.SATURDAY &&
                            dayOfWeek != java.time.DayOfWeek.SUNDAY,
                    "Should be a weekday, but was: " + dayOfWeek);
            assertEquals(9, nextDateTime.getHour(), "Should be at 9 AM");
        }

        @Test
        void testCronExpression_EverySecond() {
            // "* * * * * ?" - Every second
            long beforeCall = System.currentTimeMillis();
            long nextStart = CronUtil.nextStartTimeCronExpression("* * * * * ?");
            long afterCall = System.currentTimeMillis();

            // Should be very soon (within next second)
            assertTrue(nextStart >= beforeCall && nextStart <= afterCall + 1000,
                    "Should be within the next second");
        }
    }
}
