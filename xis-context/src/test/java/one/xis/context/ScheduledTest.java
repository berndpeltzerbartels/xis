package one.xis.context;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ScheduledTest {

    @Test
    void scheduledMethodIsRegisteredAndInvoked() throws InterruptedException {
        InvokedComponent.latch = new CountDownLatch(1);

        AppContext context = AppContext.builder()
                .withSingletonClass(InvokedComponent.class)
                .build();
        Scheduler scheduler = context.getSingleton(Scheduler.class);

        try {
            assertEquals(1, scheduler.registeredMethodCount());
            assertTrue(InvokedComponent.latch.await(1, TimeUnit.SECONDS));
        } finally {
            scheduler.shutdown();
        }
    }

    @Test
    void schedulerIsAvailableAsContextSingleton() {
        AppContext context = AppContext.builder()
                .withSingletonClass(SchedulerConsumer.class)
                .build();
        Scheduler scheduler = context.getSingleton(Scheduler.class);
        SchedulerConsumer consumer = context.getSingleton(SchedulerConsumer.class);

        try {
            assertSame(scheduler, consumer.scheduler);
        } finally {
            scheduler.shutdown();
        }
    }

    @Test
    void scheduledMethodCanUseExplicitTimeUnit() throws InterruptedException {
        TimeUnitComponent.latch = new CountDownLatch(1);

        AppContext context = AppContext.builder()
                .withSingletonClass(TimeUnitComponent.class)
                .build();
        Scheduler scheduler = context.getSingleton(Scheduler.class);

        try {
            assertTrue(TimeUnitComponent.latch.await(1, TimeUnit.SECONDS));
        } finally {
            scheduler.shutdown();
        }
    }

    @Test
    void scheduledMethodMustDefineExactlyOneInterval() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> AppContext.builder()
                .withSingletonClass(MissingIntervalComponent.class)
                .build());

        assertTrue(exception.getMessage().contains("exactly one positive interval"));
    }

    @Test
    void scheduledMethodMustNotHaveParameters() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> AppContext.builder()
                .withSingletonClass(ParameterComponent.class)
                .build());

        assertTrue(exception.getMessage().contains("must not have parameters"));
    }

    static class InvokedComponent {
        static CountDownLatch latch;

        @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MILLISECONDS)
        void tick() {
            latch.countDown();
        }
    }

    static class SchedulerConsumer {
        @Inject
        Scheduler scheduler;
    }

    static class TimeUnitComponent {
        static CountDownLatch latch;

        @Scheduled(initialDelay = 1, fixedDelay = 1, timeUnit = TimeUnit.MILLISECONDS)
        void tick() {
            latch.countDown();
        }
    }

    static class MissingIntervalComponent {
        @Scheduled(timeUnit = TimeUnit.MILLISECONDS)
        void tick() {
        }
    }

    static class ParameterComponent {
        @Scheduled(fixedDelay = 100, timeUnit = TimeUnit.MILLISECONDS)
        void tick(String value) {
        }
    }
}
