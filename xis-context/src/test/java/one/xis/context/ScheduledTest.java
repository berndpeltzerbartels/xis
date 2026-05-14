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

        @Scheduled(fixedDelayMillis = 10)
        void tick() {
            latch.countDown();
        }
    }

    static class SchedulerConsumer {
        @Inject
        Scheduler scheduler;
    }

    static class MissingIntervalComponent {
        @Scheduled
        void tick() {
        }
    }

    static class ParameterComponent {
        @Scheduled(fixedDelayMillis = 100)
        void tick(String value) {
        }
    }
}
