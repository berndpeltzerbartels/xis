package one.xis.ajax;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Phase {
    INIT, SHOW, HIDE, DESTROY;
}
