package one.xis.context.initmethod;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

@XISComponent
@Getter
@RequiredArgsConstructor
class Comp2 {
    int getInt() {
        return 3;
    }
}
