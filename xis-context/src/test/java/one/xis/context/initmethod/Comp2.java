package one.xis.context.initmethod;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;

@Component
@Getter
@RequiredArgsConstructor
class Comp2 {
    int getInt() {
        return 3;
    }
}
