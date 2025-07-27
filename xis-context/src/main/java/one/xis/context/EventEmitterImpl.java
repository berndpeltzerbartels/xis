package one.xis.context;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class EventEmitterImpl implements EventEmitter {

    private final EventDispatcher eventDispatcher;

    @Override
    public void emitEvent(Object eventData) {
        eventDispatcher.dispatchEvent(eventData);
    }
}
