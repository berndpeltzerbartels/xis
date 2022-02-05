package one.xis.remote.js2;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class JSReturn implements JSStatement2 {
    private final String value;

    JSReturn(JSStatementPart part) {
        value = part.getRef();
    }
}
