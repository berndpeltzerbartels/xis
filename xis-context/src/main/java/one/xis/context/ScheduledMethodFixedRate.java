package one.xis.context;


import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

@RequiredArgsConstructor
class ScheduledMethodFixedRate {
    private final Object bean;
    private final Method method;
    private long firstStartTimeMillis = -1;

    void start() {
        if (firstStartTimeMillis == -1) {
            firstStartTimeMillis = System.currentTimeMillis();
        }
    }


    private void done() {

    }

    private long timeMillisUntilNextStart() {
        long nowMillis = System.currentTimeMillis();
        Scheduled scheduled = method.getAnnotation(Scheduled.class);
        long fixedRateMillis = scheduled.fixedRateMillis();
        long elapsedSinceFirstStartMillis = nowMillis - firstStartTimeMillis;
        long periodsElapsed = elapsedSinceFirstStartMillis / fixedRateMillis;
        long nextStartTimeMillis = firstStartTimeMillis + (periodsElapsed + 1) * fixedRateMillis;
        long delayMillis = nextStartTimeMillis - nowMillis;
        // In case execution time was too long, we start immediately
        return Math.max(0, delayMillis);
    }
}
