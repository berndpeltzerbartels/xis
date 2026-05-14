package one.xis.context;

import lombok.extern.slf4j.Slf4j;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Runtime component that executes methods annotated with {@link Scheduled}.
 * <p>
 * The XIS context creates this component automatically. Application components can inject it if they need to stop
 * scheduled work explicitly, but normal applications only use {@link Scheduled} on component methods.
 */
@Slf4j
public class Scheduler {

    private final List<ScheduledMethod> methods = new ArrayList<>();
    private final List<ScheduledFuture<?>> futures = new ArrayList<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new SchedulerThreadFactory());
    private boolean started;

    void register(SingletonWrapper singleton, Method method) {
        methods.add(new ScheduledMethod(singleton, method));
    }

    void start() {
        if (started) {
            return;
        }
        started = true;
        for (ScheduledMethod method : methods) {
            futures.add(method.schedule(executor));
        }
    }

    int registeredMethodCount() {
        return methods.size();
    }

    /**
     * Stops all scheduled invocations and interrupts currently running scheduled work.
     */
    public void shutdown() {
        futures.forEach(future -> future.cancel(true));
        executor.shutdownNow();
    }

    private static final class ScheduledMethod {
        private final SingletonWrapper singleton;
        private final Method method;
        private final Scheduled scheduled;

        private ScheduledMethod(SingletonWrapper singleton, Method method) {
            this.singleton = singleton;
            this.method = method;
            this.scheduled = method.getAnnotation(Scheduled.class);
        }

        private ScheduledFuture<?> schedule(ScheduledExecutorService executor) {
            long initialDelay = Math.max(0, scheduled.initialDelay());
            if (scheduled.fixedRateMillis() > 0) {
                return executor.scheduleAtFixedRate(this::invoke, initialDelay, scheduled.fixedRateMillis(), TimeUnit.MILLISECONDS);
            }
            return executor.scheduleWithFixedDelay(this::invoke, initialDelay, scheduled.fixedDelayMillis(), TimeUnit.MILLISECONDS);
        }

        private void invoke() {
            try {
                MethodUtils.invoke(singleton.getTarget(), method);
            } catch (InvocationTargetException e) {
                log.error("Scheduled method {} failed", method, e.getTargetException());
            } catch (RuntimeException e) {
                log.error("Scheduled method {} failed", method, e);
            }
        }
    }

    private static final class SchedulerThreadFactory implements ThreadFactory {
        private int counter;

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "xis-scheduler-" + ++counter);
            thread.setDaemon(true);
            return thread;
        }
    }
}
