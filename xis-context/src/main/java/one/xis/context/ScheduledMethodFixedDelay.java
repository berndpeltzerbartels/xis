package one.xis.context;


import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

@RequiredArgsConstructor
class ScheduledMethodFixedDelay {
    private final Object bean;
    private final Method method;
    private Long firstStartTimeMillis;

    void start() {
     
    }


    private void done() {

    }

    private long timeMillisUntilNextStart() {
        long nowMillis = System.currentTimeMillis();
        Scheduled scheduled = method.getAnnotation(Scheduled.class);
        long fixedDelayMillis = scheduled.fixedDelayMillis();
        
        if (fixedDelayMillis <= 0) {
            throw new IllegalStateException("fixedDelayMillis must be > 0 for method " + method);
        }
        
        // fixedDelay starts counting AFTER the task completes
        // Simply return the delay - it will be added to the completion time
        return Math.max(0, fixedDelayMillis);
    }
}
