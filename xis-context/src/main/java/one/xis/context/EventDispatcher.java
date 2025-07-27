package one.xis.context;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@XISComponent
@RequiredArgsConstructor
class EventDispatcher {

    private final Collection<EventListenerMethod> eventListenerMethods = new ArrayList<>();

    void addEventListenerMethod(EventListenerMethod method) {
        eventListenerMethods.add(method);
    }

    void dispatchEvent(Object eventData) {
        for (EventListenerMethod method : eventListenerMethods) {
            if (method.matches(eventData)) {
                method.invoke(eventData);
            }
        }
    }
}
