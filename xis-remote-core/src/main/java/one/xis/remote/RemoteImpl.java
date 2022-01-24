package one.xis.remote;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class RemoteImpl<T> extends Remote<T> {
    private final T component;

    @Override
    public boolean commit() {
        return true;
    }
}
