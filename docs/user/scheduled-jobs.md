# Scheduled Jobs

[Documentation map](../README.md)

Use `@Scheduled` when a XIS Boot application needs periodic background work. Typical examples are reminder delivery,
cleanup jobs, polling small external systems, or cache refreshes.

XIS creates a `Scheduler` component automatically during context startup. After all components are constructed,
dependencies are injected, and initialization methods have run, the scheduler registers and starts all methods annotated
with `@Scheduled`.

```java
import one.xis.context.Component;
import one.xis.context.Scheduled;

import java.util.concurrent.TimeUnit;

@Component
public class ReminderJob {

    private final ReminderService reminderService;

    public ReminderJob(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(initialDelay = 1, fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    void sendDueReminders() {
        reminderService.sendDueReminders();
    }
}
```

Scheduled methods must be parameterless `void` methods. They run on the original component instance, so constructor
injection, field injection, `@Value`, and `@Init` are already available when the first invocation happens.

## Fixed Delay And Fixed Rate

Define exactly one interval:

- `fixedDelay`: waits after one invocation has finished before starting the next one.
- `fixedRate`: starts invocations at a regular interval measured from start to start.

`initialDelay` delays only the first invocation. `timeUnit` is required intentionally, so the unit is visible at the
call site.

```java
@Scheduled(initialDelay = 5, fixedRate = 10, timeUnit = TimeUnit.SECONDS)
void refreshDashboardCache() {
}
```

If a scheduled invocation throws an exception, XIS logs the error and keeps the scheduler alive for later invocations.

## Scheduler Component

The scheduler is a normal XIS context singleton. Applications usually do not need it directly, but it can be injected
when infrastructure code wants to stop scheduled work explicitly.

```java
import one.xis.context.Inject;
import one.xis.context.Scheduler;

public class ShutdownHook {

    @Inject
    Scheduler scheduler;

    void stop() {
        scheduler.shutdown();
    }
}
```

Cron-style schedules are not implemented yet. Use `fixedDelay` or `fixedRate` for now.
