package one.xis.context;

/**
 * Publishes application events to methods annotated with {@link EventListener}.
 */
public interface EventEmitter {
    /**
     * Emits an event object to matching listener methods.
     */
    void emitEvent(Object eventData);
}
