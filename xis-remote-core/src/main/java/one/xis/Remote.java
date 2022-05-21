package one.xis;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class Remote<T> {

    abstract T getComponent();

    public static <R> Remote<R> getRemote(Class<R> componentClass, String clientId) {
        return new RemoteImpl<>(null);
    }

    abstract boolean commit();
}
