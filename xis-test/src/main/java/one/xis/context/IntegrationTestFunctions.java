package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class IntegrationTestFunctions {
    private final JavascriptFunction invoker;
    private final JavascriptFunction reset;
}
